package com.translatr.controller;

import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.dto.PagedList;
import com.translatr.service.AccessTokenService;
import com.translatr.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccessTokenResource {

    private final AccessTokenService tokenService;
    private final UserService        userService;
    private final JsonWebToken       jwt;

    @Inject
    public AccessTokenResource(AccessTokenService tokenService, UserService userService, JsonWebToken jwt) {
        this.tokenService = tokenService;
        this.userService  = userService;
        this.jwt          = jwt;
    }

    @GET @Path("/accesstokens")
    public PagedList<AccessTokenDto> find(@BeanParam AccessTokenCriteria criteria) {
        var owner = userService.findOrCreate("oidc", jwt.getSubject(),
                jwt.getClaim("name"), jwt.getClaim("email"));
        return tokenService.find(criteria, owner.id);
    }

    @GET @Path("/accesstoken/{id}")
    public AccessTokenDto get(@PathParam("id") Long id) { return tokenService.get(id); }

    @POST @Path("/accesstoken")
    public AccessTokenDto create(AccessTokenDto dto) {
        var owner = userService.findOrCreate("oidc", jwt.getSubject(),
                jwt.getClaim("name"), jwt.getClaim("email"));
        return tokenService.create(dto, owner);
    }

    @PUT @Path("/accesstoken")
    public AccessTokenDto update(AccessTokenDto dto) { return tokenService.update(dto); }

    @DELETE @Path("/accesstoken/{id}")
    public AccessTokenDto delete(@PathParam("id") Long id) { return tokenService.delete(id); }
}
