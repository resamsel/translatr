package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.MessageCriteria;
import com.translatr.dto.MessageDto;
import com.translatr.dto.MessagePayload;
import com.translatr.dto.PagedList;
import com.translatr.dto.PagedMessageList;
import com.translatr.generated.api.MessagesApi;
import com.translatr.service.MessageService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class MessageResource implements MessagesApi {

    private final MessageService     messageService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public MessageResource(MessageService messageService, CurrentUserResolver currentUserResolver) {
        this.messageService      = messageService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * The language a message's {@code localeDisplayName} should be rendered in: the signed-in
     * user's preferred language, or English for an anonymous caller / a user who never picked
     * one. Twin of {@code LocaleResource.viewerLocale()}.
     */
    private java.util.Locale viewerLocale() {
        return currentUserResolver.resolveOptional()
                .map(u -> u.preferredLocale)
                .filter(tag -> tag != null && !tag.isBlank())
                .map(java.util.Locale::forLanguageTag)
                .orElse(java.util.Locale.ENGLISH);
    }

    @Override
    @PermitAll
    public PagedMessageList findMessages(String search, Integer offset, Integer limit, String order, String fetch,
                                          UUID projectId, UUID localeId, String localeIds, UUID keyId,
                                          String keyIds, String keyName) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, localeId, localeIds, keyId,
                keyIds, keyName);
        return toPagedDto(messageService.find(criteria, viewerLocale()));
    }

    @Override
    @PermitAll
    public PagedMessageList findMessagesByProject(UUID projectId, String search, Integer offset, Integer limit,
                                                   String order, String fetch, UUID localeId, String localeIds,
                                                   UUID keyId, String keyIds, String keyName) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, localeId, localeIds, keyId,
                keyIds, keyName);
        return toPagedDto(messageService.find(criteria, viewerLocale()));
    }

    @Override
    @PermitAll
    public MessagePayload getMessage(UUID id) {
        return toApiDto(messageService.get(id, viewerLocale()));
    }

    @Override
    @Authenticated
    public MessagePayload createMessage(MessagePayload messagePayload) {
        return toApiDto(messageService.create(toServiceDto(messagePayload)));
    }

    @Override
    @Authenticated
    public MessagePayload updateMessage(MessagePayload messagePayload) {
        return toApiDto(messageService.update(toServiceDto(messagePayload)));
    }

    @Override
    @Authenticated
    public MessagePayload deleteMessage(UUID id) {
        return toApiDto(messageService.delete(id));
    }

    static MessageCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                       UUID projectId, UUID localeId, String localeIds, UUID keyId, String keyIds,
                                       String keyName) {
        MessageCriteria c = new MessageCriteria();
        c.search    = search;
        c.offset    = offset;
        c.limit     = limit;
        c.order     = order;
        c.fetch     = fetch;
        c.projectId = projectId;
        c.localeId  = localeId;
        c.localeIds = localeIds;
        c.keyId     = keyId;
        c.keyIds    = keyIds;
        c.keyName   = keyName;
        return c;
    }

    private static PagedMessageList toPagedDto(PagedList<MessageDto> src) {
        return new PagedMessageList(
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev,
                src.list.stream().map(MessageResource::toApiDto).toList());
    }

    private static MessagePayload toApiDto(MessageDto d) {
        return new MessagePayload()
                .id(d.id)
                .whenCreated(toOffsetDateTime(d.whenCreated))
                .whenUpdated(toOffsetDateTime(d.whenUpdated))
                .localeId(d.localeId)
                .localeName(d.localeName)
                .localeDisplayName(d.localeDisplayName)
                .keyId(d.keyId)
                .keyName(d.keyName)
                .projectId(d.projectId)
                .projectName(d.projectName)
                .value(d.value)
                .wordCount(d.wordCount);
    }

    private static OffsetDateTime toOffsetDateTime(Instant i) {
        return i == null ? null : i.atOffset(ZoneOffset.UTC);
    }

    private static MessageDto toServiceDto(MessagePayload p) {
        MessageDto d = new MessageDto();
        d.id       = p.getId();
        d.localeId = p.getLocaleId();
        d.keyId    = p.getKeyId();
        d.value    = p.getValue();
        return d;
    }
}
