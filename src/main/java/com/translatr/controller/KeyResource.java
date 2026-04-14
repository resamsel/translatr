package com.translatr.controller;

import com.translatr.criteria.KeyCriteria;
import com.translatr.dto.KeyDto;
import com.translatr.dto.PagedList;
import com.translatr.service.KeyService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeyResource {

    private final KeyService keyService;

    @Inject
    public KeyResource(KeyService keyService) {
        this.keyService = keyService;
    }

    @GET  @Path("/project/{projectId}/keys") @PermitAll
    public PagedList<KeyDto> findByProject(@PathParam("projectId") UUID projectId,
                                           @BeanParam KeyCriteria criteria) {
        criteria.projectId = projectId;
        return keyService.find(criteria);
    }

    @GET  @Path("/key/{id}")     @PermitAll
    public KeyDto get(@PathParam("id") UUID id) { return keyService.get(id); }

    @GET  @Path("/{username}/{projectName}/keys/{keyName}")  @PermitAll
    public KeyDto getByOwnerAndProjectNameAndName(
            @PathParam("username")    String username,
            @PathParam("projectName") String projectName,
            @PathParam("keyName")     String keyName) {
        return keyService.getByOwnerAndProjectNameAndName(username, projectName, keyName);
    }

    @POST @Path("/key")          @Authenticated
    public KeyDto create(KeyDto dto) { return keyService.create(dto); }

    @PUT  @Path("/key")          @Authenticated
    public KeyDto update(KeyDto dto) { return keyService.update(dto); }

    @DELETE @Path("/key/{id}")   @Authenticated
    public KeyDto delete(@PathParam("id") UUID id) { return keyService.delete(id); }
}
