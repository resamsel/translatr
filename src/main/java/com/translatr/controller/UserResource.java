package com.translatr.controller;

import com.translatr.criteria.UserCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.UserDto;
import com.translatr.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject UserService  userService;
    @Inject JsonWebToken jwt;

    @GET  @Path("/users")         @PermitAll
    public PagedList<UserDto> find(@BeanParam UserCriteria criteria) {
        return userService.find(criteria);
    }

    @GET  @Path("/user/{id}")     @PermitAll
    public UserDto get(@PathParam("id") UUID id) { return userService.get(id); }

    @GET  @Path("/api/{username}") @PermitAll
    public UserDto byUsername(@PathParam("username") String username) {
        return userService.getByUsername(username);
    }

    @GET  @Path("/me")            @Authenticated
    public UserDto me() {
        return userService.findOrCreate("oidc", jwt.getSubject(),
                jwt.getClaim("name"), jwt.getClaim("email"))
                .id != null ? userService.get(
                    userService.findOrCreate("oidc", jwt.getSubject(),
                    jwt.getClaim("name"), jwt.getClaim("email")).id) : null;
    }

    @GET  @Path("/profile")       @PermitAll
    public UserDto profile() { return null; }

    @PUT  @Path("/user")          @Authenticated
    public UserDto update(UserDto dto) { return userService.update(dto); }

    @DELETE @Path("/user/{id}")   @Authenticated
    public UserDto delete(@PathParam("id") UUID id) { return userService.delete(id); }

    @PUT  @Path("/user/{id}/settings")   @Authenticated
    public UserDto saveSettings(@PathParam("id") UUID id, UserDto dto) {
        dto.id = id;
        return userService.update(dto);
    }
}
