package com.translatr.service;

import com.translatr.criteria.ProjectCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.ProjectDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.Project;
import com.translatr.model.User;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectService {

    @Inject ProjectRepository projectRepo;
    @Inject UserRepository    userRepo;
    @Inject DtoMapper         mapper;

    public PagedList<ProjectDto> find(ProjectCriteria c) {
        var query = projectRepo.find(
            "deleted = false AND (owner.username = ?1 OR ?1 IS NULL) ORDER BY whenCreated DESC",
            c.ownerUsername);
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

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
    public ProjectDto update(ProjectDto dto) {
        Project p = projectRepo.findByIdOptional(dto.id)
                .orElseThrow(NotFoundException::new);
        if (dto.name        != null) p.name        = dto.name;
        if (dto.description != null) p.description = dto.description;
        return mapper.toDto(p);
    }

    @Transactional
    public ProjectDto delete(UUID id) {
        Project p = projectRepo.findByIdOptional(id)
                .orElseThrow(NotFoundException::new);
        p.deleted = true;
        return mapper.toDto(p);
    }
}
