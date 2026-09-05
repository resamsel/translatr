package com.translatr.service;

import com.translatr.criteria.MessageCriteria;
import com.translatr.dto.MessageDto;
import com.translatr.dto.PagedList;
import com.translatr.event.WordCountEventProducer;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Message;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.LocaleRepository;
import com.translatr.repository.MessageRepository;
import com.translatr.util.LocaleDisplayNameUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class MessageService {

    private final MessageRepository messageRepo;
    private final LocaleRepository  localeRepo;
    private final KeyRepository     keyRepo;
    private final DtoMapper         mapper;
    private final ActivityLogger    activity;
    private final WordCountEventProducer wordCounts;

    @Inject
    public MessageService(MessageRepository messageRepo, LocaleRepository localeRepo,
                          KeyRepository keyRepo, DtoMapper mapper, ActivityLogger activity,
                          WordCountEventProducer wordCounts) {
        this.messageRepo = messageRepo;
        this.localeRepo  = localeRepo;
        this.keyRepo     = keyRepo;
        this.mapper      = mapper;
        this.activity    = activity;
        this.wordCounts  = wordCounts;
    }

    private static final List<String> ORDERABLE = List.of("key.name", "value", "whenCreated", "whenUpdated");

    /**
     * Stamps {@link MessageDto#localeDisplayName} - the human name of {@code dto.localeName}
     * rendered in {@code viewerLocale} (the signed-in user's preferred language, resolved by the
     * resource; never {@code null}). Mirrors {@code LocaleService.stampDisplayName}; like there,
     * only the read paths stamp it - create/update/delete responses and activity-log snapshots
     * leave it null.
     */
    private void stampLocaleDisplayName(MessageDto dto, java.util.Locale viewerLocale) {
        dto.localeDisplayName = LocaleDisplayNameUtils.formatDisplayName(dto.localeName, viewerLocale);
    }

    public PagedList<MessageDto> find(MessageCriteria c, java.util.Locale viewerLocale) {
        // Port of the old MessageRepositoryImpl.findBy: every criteria field that is set
        // narrows the result, combined with AND. Dropping any of them (as the first
        // Quarkus cut did) makes e.g. the key editor's
        // ?projectId=..&keyName=..&localeIds=.. request return page 0 of *all* project
        // messages, so it shows a different key's translations.
        StringBuilder ql    = new StringBuilder("1 = 1");
        List<Object>  params = new ArrayList<>();

        if (c.projectId != null) {
            params.add(c.projectId);
            ql.append(" AND locale.project.id = ?").append(params.size());
        }
        if (c.localeId != null) {
            params.add(c.localeId);
            ql.append(" AND locale.id = ?").append(params.size());
        }
        List<UUID> localeIds = QuerySupport.uuidCsv(c.localeIds);
        if (!localeIds.isEmpty()) {
            params.add(localeIds);
            ql.append(" AND locale.id IN ?").append(params.size());
        }
        if (c.keyId != null) {
            params.add(c.keyId);
            ql.append(" AND key.id = ?").append(params.size());
        }
        List<UUID> keyIds = QuerySupport.uuidCsv(c.keyIds);
        if (!keyIds.isEmpty()) {
            params.add(keyIds);
            ql.append(" AND key.id IN ?").append(params.size());
        }
        if (QuerySupport.hasText(c.keyName)) {
            params.add(c.keyName);
            ql.append(" AND key.name = ?").append(params.size());
        }
        if (QuerySupport.hasText(c.search)) {
            params.add(QuerySupport.like(c.search));
            ql.append(" AND lower(value) LIKE ?").append(params.size());
        }
        ql.append(' ').append(QuerySupport.orderBy(c.order, ORDERABLE, "ORDER BY key.name"));

        var query  = messageRepo.find(ql.toString(), params.toArray());
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        list.forEach(d -> stampLocaleDisplayName(d, viewerLocale));
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public MessageDto get(UUID id, java.util.Locale viewerLocale) {
        MessageDto dto = mapper.toDto(messageRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
        stampLocaleDisplayName(dto, viewerLocale);
        return dto;
    }

    @Transactional
    public MessageDto create(MessageDto dto) {
        var locale = localeRepo.findByIdOptional(dto.localeId).orElseThrow(NotFoundException::new);
        var key    = keyRepo.findByIdOptional(dto.keyId).orElseThrow(NotFoundException::new);
        Message m  = new Message(locale, key, dto.value);
        messageRepo.persist(m);
        MessageDto after = mapper.toDto(m);
        activity.publish(ActionType.Create, key.project, MessageDto.class, null, after);
        publishWordCountEvents(m);
        return after;
    }

    @Transactional
    public MessageDto update(MessageDto dto) {
        Message m = messageRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        MessageDto before = mapper.toDto(m);
        if (dto.value != null) m.value = dto.value;
        MessageDto after = mapper.toDto(m);
        activity.publish(ActionType.Update, m.key != null ? m.key.project : null,
                MessageDto.class, before, after);
        publishWordCountEvents(m);
        return after;
    }

    @Transactional
    public MessageDto delete(UUID id) {
        Message m = messageRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        MessageDto before = mapper.toDto(m);
        var project    = m.key != null ? m.key.project : null;
        UUID keyId     = m.key    != null ? m.key.id    : null;
        UUID localeId  = m.locale != null ? m.locale.id : null;
        UUID projectId = project  != null ? project.id  : null;
        messageRepo.delete(m);
        activity.publish(ActionType.Delete, project, MessageDto.class, before, null);
        // the message row is gone — refresh only the roll-ups it fed into
        if (keyId     != null) wordCounts.publishKey(keyId);
        if (localeId  != null) wordCounts.publishLocale(localeId);
        if (projectId != null) wordCounts.publishProject(projectId);
        return before;
    }

    /**
     * Recalculate word counts after a message was created or its value changed.
     * Ports the old {@code MessageServiceImpl.preSave -> messageWordCountActor.tell(...)}
     * fan-out: {@code WordCountEventConsumer} refreshes the message itself and then its
     * key, locale and project roll-ups (processed in that order).
     */
    private void publishWordCountEvents(Message m) {
        wordCounts.publishMessage(m.id);
        if (m.key    != null) wordCounts.publishKey(m.key.id);
        if (m.locale != null) wordCounts.publishLocale(m.locale.id);
        if (m.key != null && m.key.project != null) wordCounts.publishProject(m.key.project.id);
    }
}
