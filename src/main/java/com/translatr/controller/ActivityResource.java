package com.translatr.controller;

import com.translatr.dto.ActivityDto;
import com.translatr.dto.PagedList;
import com.translatr.service.ActivityService;
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
public class ActivityResource {

    @Inject ActivityService activityService;
    @Inject UserService     userService;
    @Inject JsonWebToken    jwt;

    @GET @Path("/activities") @PermitAll
    public PagedList<ActivityDto> find(@QueryParam("offset") @DefaultValue("0") int offset,
                                       @QueryParam("limit")  @DefaultValue("20") int limit) {
        var user = userService.findOrCreate("oidc", jwt.getSubject(),
                jwt.getClaim("name"), jwt.getClaim("email"));
        return activityService.findByUser(user.id, offset, limit);
    }

    @GET @Path("/user/{userId}/activity") @PermitAll
    public PagedList<ActivityDto> byUser(@PathParam("userId") UUID userId,
                                         @QueryParam("offset") @DefaultValue("0") int offset,
                                         @QueryParam("limit")  @DefaultValue("20") int limit) {
        return activityService.findByUser(userId, offset, limit);
    }
}
