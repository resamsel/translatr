package com.translatr.service;

import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.ProjectDto;
import com.translatr.dto.MemberDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Project;
import com.translatr.model.ProjectRole;
import com.translatr.model.ProjectUser;
import com.translatr.model.User;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.ProjectUserRepository;
import com.translatr.repository.UserRepository;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectService {

    private final ProjectRepository     projectRepo;
    private final UserRepository        userRepo;
    private final ProjectUserRepository memberRepo;
    private final DtoMapper             mapper;
    private final ActivityLogger        activity;

    @Inject
    public ProjectService(ProjectRepository projectRepo, UserRepository userRepo,
                           ProjectUserRepository memberRepo, DtoMapper mapper,
                           ActivityLogger activity) {
        this.projectRepo = projectRepo;
        this.userRepo    = userRepo;
        this.memberRepo  = memberRepo;
        this.mapper      = mapper;
        this.activity    = activity;
    }

    private static boolean wants(String fetch, String association) {
        return fetch != null && Arrays.asList(fetch.split(",")).contains(association);
    }

    private void fetchMembers(ProjectDto dto) {
        dto.members = memberRepo.list("project.id", dto.id)
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    private static final List<String> ORDERABLE =
        List.of("name", "whenCreated", "whenUpdated", "wordCount");

    public PagedList<ProjectDto> find(ProjectCriteria c) {
        // Port of ProjectRepositoryImpl.findBy: apply every populated criteria field, not just ownerUsername.
        StringBuilder ql    = new StringBuilder("FROM Project p WHERE p.deleted = false");
        List<Object>  params = new ArrayList<>();

        if (c.ownerId != null) {
            params.add(c.ownerId);
            ql.append(" AND p.owner.id = ?").append(params.size());
        }
        if (QuerySupport.hasText(c.ownerUsername)) {
            params.add(c.ownerUsername);
            ql.append(" AND p.owner.username = ?").append(params.size());
        }
        if (c.memberId != null) {
            params.add(c.memberId);
            ql.append(" AND EXISTS (SELECT 1 FROM ProjectUser pu WHERE pu.project = p AND pu.user.id = ?")
              .append(params.size()).append(')');
        }
        if (QuerySupport.hasText(c.name)) {
            params.add(c.name);
            ql.append(" AND p.name = ?").append(params.size());
        }
        if (QuerySupport.hasText(c.search)) {
            params.add(QuerySupport.like(c.search));
            int i = params.size();
            ql.append(" AND (lower(p.name) LIKE ?").append(i)
              .append(" OR lower(p.description) LIKE ?").append(i)
              .append(" OR lower(p.owner.name) LIKE ?").append(i)
              .append(" OR lower(p.owner.username) LIKE ?").append(i).append(')');
        }
        ql.append(' ').append(QuerySupport.orderBy(c.order, ORDERABLE, "ORDER BY whenCreated DESC"));

        var query  = projectRepo.find(ql.toString(), params.toArray());
        long total = query.count();
        List<ProjectDto> list = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        if (wants(c.fetch, "members")) {
            list.forEach(this::fetchMembers);
        }
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    @CacheResult(cacheName = "projects")
    public ProjectDto get(UUID id) {
        return mapper.toDto(projectRepo.findByIdOptional(id)
                .orElseThrow(NotFoundException::new));
    }

    public ProjectDto getByOwnerAndName(String username, String name) {
        return mapper.toDto(projectRepo.findByOwnerUsernameAndName(username, name)
                .orElseThrow(NotFoundException::new));
    }

    @Transactional
    public ProjectDto create(ProjectDto dto, User owner) {
        Project p = new Project(dto.name);
        p.description = dto.description;
        p.owner       = owner;
        projectRepo.persist(p);

        // The owner is always a member of their project.
        ProjectUser ownerMember = new ProjectUser(ProjectRole.Owner);
        ownerMember.project = p;
        ownerMember.user    = owner;
        memberRepo.persist(ownerMember);

        ProjectDto  after       = mapper.toDto(p);
        MemberDto   memberAfter = mapper.toDto(ownerMember);
        activity.publish(ActionType.Create, p, ProjectDto.class, null, after);
        activity.publish(ActionType.Create, p, MemberDto.class, null, memberAfter);
        return after;
    }

    @Transactional
    @CacheInvalidate(cacheName = "projects")
    public ProjectDto update(ProjectDto dto) {
        Project p = projectRepo.findByIdOptional(dto.id)
                .orElseThrow(NotFoundException::new);
        ProjectDto before = mapper.toDto(p);
        if (dto.name        != null) p.name        = dto.name;
        if (dto.description != null) p.description = dto.description;
        ProjectDto after = mapper.toDto(p);
        activity.publish(ActionType.Update, p, ProjectDto.class, before, after);
        return after;
    }

    @Transactional
    @CacheInvalidate(cacheName = "projects")
    public ProjectDto delete(UUID id) {
        Project p = projectRepo.findByIdOptional(id)
                .orElseThrow(NotFoundException::new);
        ProjectDto before = mapper.toDto(p);
        p.deleted = true;
        activity.publish(ActionType.Delete, p, ProjectDto.class, before, null);
        return mapper.toDto(p);
    }
}
