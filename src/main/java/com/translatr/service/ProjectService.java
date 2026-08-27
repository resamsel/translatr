package com.translatr.service;

import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.ProjectDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.Project;
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

    @Inject
    public ProjectService(ProjectRepository projectRepo, UserRepository userRepo,
                           ProjectUserRepository memberRepo, DtoMapper mapper) {
        this.projectRepo = projectRepo;
        this.userRepo    = userRepo;
        this.memberRepo  = memberRepo;
        this.mapper      = mapper;
    }

    private static boolean wants(String fetch, String association) {
        return fetch != null && Arrays.asList(fetch.split(",")).contains(association);
    }

    private void fetchMembers(ProjectDto dto) {
        dto.members = memberRepo.list("project.id", dto.id)
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PagedList<ProjectDto> find(ProjectCriteria c) {
        var query = projectRepo.find(
            "deleted = false AND (owner.username = ?1 OR ?1 IS NULL) ORDER BY whenCreated DESC",
            c.ownerUsername);
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
        return mapper.toDto(p);
    }

    @Transactional
    @CacheInvalidate(cacheName = "projects")
    public ProjectDto update(ProjectDto dto) {
        Project p = projectRepo.findByIdOptional(dto.id)
                .orElseThrow(NotFoundException::new);
        if (dto.name        != null) p.name        = dto.name;
        if (dto.description != null) p.description = dto.description;
        return mapper.toDto(p);
    }

    @Transactional
    @CacheInvalidate(cacheName = "projects")
    public ProjectDto delete(UUID id) {
        Project p = projectRepo.findByIdOptional(id)
                .orElseThrow(NotFoundException::new);
        p.deleted = true;
        return mapper.toDto(p);
    }
}
