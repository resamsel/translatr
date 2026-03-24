package com.translatr.controller;

import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.ProjectDto;
import com.translatr.service.ProjectService;
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
public class ProjectResource {

    @Inject ProjectService projectService;
    @Inject UserService    userService;
    @Inject JsonWebToken   jwt;

    @GET
    @Path("/projects")
    @PermitAll
    public PagedList<ProjectDto> find(@BeanParam ProjectCriteria criteria) {
        return projectService.find(criteria);
    }

    @GET
    @Path("/project/{id}")
    @PermitAll
    public ProjectDto get(@PathParam("id") UUID id) {
        return projectService.get(id);
    }

    @GET
    @Path("/{username}/{projectName}")
    @PermitAll
    public ProjectDto getByOwnerAndName(@PathParam("username") String username,
                                        @PathParam("projectName") String name) {
        return projectService.getByOwnerAndName(username, name);
    }

    @POST
    @Path("/project")
    @Authenticated
    public ProjectDto create(ProjectDto dto) {
        var owner = userService.findOrCreate(
            "oidc", jwt.getSubject(), jwt.getClaim("name"), jwt.getClaim("email"));
        return projectService.create(dto, owner);
    }

    @PUT
    @Path("/project")
    @Authenticated
    public ProjectDto update(ProjectDto dto) {
        return projectService.update(dto);
    }

    @DELETE
    @Path("/project/{id}")
    @Authenticated
    public ProjectDto delete(@PathParam("id") UUID id) {
        return projectService.delete(id);
    }
}
