package com.translatr.service;

import com.translatr.criteria.MessageCriteria;
import com.translatr.dto.MessageDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Message;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.LocaleRepository;
import com.translatr.repository.MessageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class MessageService {

    private final MessageRepository messageRepo;
    private final LocaleRepository  localeRepo;
    private final KeyRepository     keyRepo;
    private final DtoMapper         mapper;
    private final ActivityLogger    activity;

    @Inject
    public MessageService(MessageRepository messageRepo, LocaleRepository localeRepo,
                          KeyRepository keyRepo, DtoMapper mapper, ActivityLogger activity) {
        this.messageRepo = messageRepo;
        this.localeRepo  = localeRepo;
        this.keyRepo     = keyRepo;
        this.mapper      = mapper;
        this.activity    = activity;
    }

    public PagedList<MessageDto> find(MessageCriteria c) {
        var query = c.projectId != null
            ? messageRepo.find("locale.project.id = ?1 ORDER BY key.name", c.projectId)
            : c.localeId != null
                ? messageRepo.find("locale.id = ?1 ORDER BY key.name", c.localeId)
                : messageRepo.findAll();
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public MessageDto get(UUID id) {
        return mapper.toDto(messageRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
    }

    @Transactional
    public MessageDto create(MessageDto dto) {
        var locale = localeRepo.findByIdOptional(dto.localeId).orElseThrow(NotFoundException::new);
        var key    = keyRepo.findByIdOptional(dto.keyId).orElseThrow(NotFoundException::new);
        Message m  = new Message(locale, key, dto.value);
        messageRepo.persist(m);
        MessageDto after = mapper.toDto(m);
        activity.publish(ActionType.Create, key.project, MessageDto.class, null, after);
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
        return after;
    }

    @Transactional
    public MessageDto delete(UUID id) {
        Message m = messageRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        MessageDto before = mapper.toDto(m);
        var project = m.key != null ? m.key.project : null;
        messageRepo.delete(m);
        activity.publish(ActionType.Delete, project, MessageDto.class, before, null);
        return before;
    }
}
