package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.ProjectDto;
import com.translatr.service.ProjectService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

    private final ProjectService      projectService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public ProjectResource(ProjectService projectService, CurrentUserResolver currentUserResolver) {
        this.projectService      = projectService;
        this.currentUserResolver = currentUserResolver;
    }

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
                                        @PathParam("projectName") String name,
                                        @QueryParam("fetch") String fetch) {
        // Only resolve the caller when ?fetch=myrole asks for it - otherwise this @PermitAll route
        // stays free of per-request user resolution (which, for OIDC, also re-syncs the role).
        UUID loggedInUserId = fetch != null && fetch.contains("myrole")
                ? currentUserResolver.resolveOptional().map(u -> u.id).orElse(null)
                : null;
        return projectService.getByOwnerAndName(username, name, fetch, loggedInUserId);
    }

    @POST
    @Path("/project")
    @Authenticated
    public ProjectDto create(ProjectDto dto) {
        var owner = currentUserResolver.resolve();
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
