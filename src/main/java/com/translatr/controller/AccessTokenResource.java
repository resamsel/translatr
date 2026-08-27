package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.dto.PagedList;
import com.translatr.service.AccessTokenService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccessTokenResource {

    private final AccessTokenService  tokenService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public AccessTokenResource(AccessTokenService tokenService, CurrentUserResolver currentUserResolver) {
        this.tokenService        = tokenService;
        this.currentUserResolver = currentUserResolver;
    }

    @GET @Path("/accesstokens")
    public PagedList<AccessTokenDto> find(@BeanParam AccessTokenCriteria criteria) {
        var owner = currentUserResolver.resolve();
        return tokenService.find(criteria, owner.id);
    }

    @GET @Path("/accesstoken/{id}")
    public AccessTokenDto get(@PathParam("id") Long id) { return tokenService.get(id); }

    @POST @Path("/accesstoken")
    public AccessTokenDto create(AccessTokenDto dto) {
        var owner = currentUserResolver.resolve();
        return tokenService.create(dto, owner);
    }

    @PUT @Path("/accesstoken")
    public AccessTokenDto update(AccessTokenDto dto) { return tokenService.update(dto); }

    @DELETE @Path("/accesstoken/{id}")
    public AccessTokenDto delete(@PathParam("id") Long id) { return tokenService.delete(id); }
}
