package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.Member;
import com.translatr.dto.MemberDto;
import com.translatr.dto.PagedList;
import com.translatr.dto.PagedProjectList;
import com.translatr.dto.ProjectDto;
import com.translatr.dto.ProjectPayload;
import com.translatr.generated.api.ProjectsApi;
import com.translatr.service.ProjectService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    public ProjectPayload getProject(UUID id) {
        return toApiDto(projectService.get(id));
    }

    @Override
    @PermitAll
    public ProjectPayload getProjectByOwnerAndName(String username, String projectName, String fetch) {
        UUID loggedInUserId = fetch != null && fetch.contains("myrole")
                ? currentUserResolver.resolveOptional().map(u -> u.id).orElse(null)
                : null;
        return toApiDto(projectService.getByOwnerAndName(username, projectName, fetch, loggedInUserId));
    }

    @Override
    @Authenticated
    public ProjectPayload createProject(ProjectPayload projectPayload) {
        var owner = currentUserResolver.resolve();
        return toApiDto(projectService.create(toServiceDto(projectPayload), owner));
    }

    @Override
    @Authenticated
    public ProjectPayload updateProject(ProjectPayload projectPayload) {
        return toApiDto(projectService.update(toServiceDto(projectPayload)));
    }

    @Override
    @Authenticated
    public ProjectPayload deleteProject(UUID id) {
        return toApiDto(projectService.delete(id));
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
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev,
                src.list.stream().map(ProjectResource::toApiDto).toList());
    }

    private static ProjectPayload toApiDto(ProjectDto d) {
        ProjectPayload p = new ProjectPayload()
                .id(d.id)
                .whenCreated(toOffsetDateTime(d.whenCreated))
                .whenUpdated(toOffsetDateTime(d.whenUpdated))
                .name(d.name)
                .description(d.description)
                .ownerId(d.ownerId)
                .ownerName(d.ownerName)
                .ownerUsername(d.ownerUsername)
                .ownerEmailHash(d.ownerEmailHash)
                .wordCount(d.wordCount)
                .progress(d.progress)
                .myRole(d.myRole);
        if (d.members != null) {
            p.members(d.members.stream().map(ProjectResource::toApiMember).toList());
        }
        return p;
    }

    private static Member toApiMember(MemberDto m) {
        return new Member()
                .id(m.id)
                .whenCreated(toOffsetDateTime(m.whenCreated))
                .projectId(m.projectId)
                .projectName(m.projectName)
                .userId(m.userId)
                .userUsername(m.userUsername)
                .userName(m.userName)
                .userEmailHash(m.userEmailHash)
                .role(m.role);
    }

    private static OffsetDateTime toOffsetDateTime(Instant i) {
        return i == null ? null : i.atOffset(ZoneOffset.UTC);
    }

    private static ProjectDto toServiceDto(ProjectPayload p) {
        ProjectDto d = new ProjectDto();
        d.id          = p.getId();
        d.name        = p.getName();
        d.description = p.getDescription();
        return d;
    }
}
