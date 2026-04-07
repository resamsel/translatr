package com.translatr.controller;

import com.translatr.dto.PagedList;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Collections;

/**
 * Stub implementation of the notifications endpoint.
 * The original Play application used getstream.io which is not part of the
 * Quarkus migration. Returns an empty list until a replacement is wired in.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @GET
    @Path("/notifications")
    @PermitAll
    public PagedList<Object> find(
            @QueryParam("offset") @DefaultValue("0")  int offset,
            @QueryParam("limit")  @DefaultValue("20") int limit) {
        return new PagedList<>(Collections.emptyList(), 0, offset, limit);
    }
}


