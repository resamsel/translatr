package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.PagedProjectList;
import com.translatr.dto.ProjectDto;
import com.translatr.generated.api.ProjectsApi;
import com.translatr.service.ProjectService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

import java.util.UUID;

public class ProjectResource implements ProjectsApi {

    private final ProjectService      projectService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public ProjectResource(ProjectService projectService, CurrentUserResolver currentUserResolver) {
        this.projectService      = projectService;
        this.currentUserResolver = currentUserResolver;
    }

    @Override
    @PermitAll
    public PagedProjectList findProjects(String search, Integer offset, Integer limit, String order, String fetch,
                                          UUID ownerId, String ownerUsername, UUID memberId, String name) {
        var criteria = toCriteria(search, offset, limit, order, fetch, ownerId, ownerUsername, memberId, name);
        return toPagedDto(projectService.find(criteria));
    }

    @Override
    @PermitAll
    public ProjectDto getProject(UUID id) {
        return projectService.get(id);
    }

    @Override
    @PermitAll
    public ProjectDto getProjectByOwnerAndName(String username, String projectName, String fetch) {
        UUID loggedInUserId = fetch != null && fetch.contains("myrole")
                ? currentUserResolver.resolveOptional().map(u -> u.id).orElse(null)
                : null;
        return projectService.getByOwnerAndName(username, projectName, fetch, loggedInUserId);
    }

    @Override
    @Authenticated
    public ProjectDto createProject(ProjectDto projectPayload) {
        var owner = currentUserResolver.resolve();
        return projectService.create(projectPayload, owner);
    }

    @Override
    @Authenticated
    public ProjectDto updateProject(ProjectDto projectPayload) {
        return projectService.update(projectPayload);
    }

    @Override
    @Authenticated
    public ProjectDto deleteProject(UUID id) {
        return projectService.delete(id);
    }

    static ProjectCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                       UUID ownerId, String ownerUsername, UUID memberId, String name) {
        ProjectCriteria c = new ProjectCriteria();
        c.search        = search;
        c.offset        = offset;
        c.limit         = limit;
        c.order         = order;
        c.fetch         = fetch;
        c.ownerId       = ownerId;
        c.ownerUsername = ownerUsername;
        c.memberId      = memberId;
        c.name          = name;
        return c;
    }

    private static PagedProjectList toPagedDto(PagedList<ProjectDto> src) {
        return new PagedProjectList(
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
