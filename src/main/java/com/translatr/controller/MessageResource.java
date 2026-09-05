package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.MessageCriteria;
import com.translatr.dto.MessageDto;
import com.translatr.dto.PagedList;
import com.translatr.service.MessageService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource {

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

    @GET  @Path("/project/{projectId}/messages") @PermitAll
    public PagedList<MessageDto> findByProject(@PathParam("projectId") UUID projectId,
                                               @BeanParam MessageCriteria criteria) {
        criteria.projectId = projectId;
        return messageService.find(criteria, viewerLocale());
    }

    @GET  @Path("/messages")        @PermitAll
    public PagedList<MessageDto> find(@BeanParam MessageCriteria criteria) {
        return messageService.find(criteria, viewerLocale());
    }

    @GET  @Path("/message/{id}")    @PermitAll
    public MessageDto get(@PathParam("id") UUID id) { return messageService.get(id, viewerLocale()); }

    @POST @Path("/message")         @Authenticated
    public MessageDto create(MessageDto dto) { return messageService.create(dto); }

    @PUT  @Path("/message")         @Authenticated
    public MessageDto update(MessageDto dto) { return messageService.update(dto); }

    @DELETE @Path("/message/{id}")  @Authenticated
    public MessageDto delete(@PathParam("id") UUID id) { return messageService.delete(id); }
}
