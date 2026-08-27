package com.translatr.service;

import com.translatr.criteria.KeyCriteria;
import com.translatr.dto.KeyDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Key;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class KeyService {

    private final KeyRepository     keyRepo;
    private final ProjectRepository projectRepo;
    private final DtoMapper         mapper;
    private final ActivityLogger    activity;

    @Inject
    public KeyService(KeyRepository keyRepo, ProjectRepository projectRepo, DtoMapper mapper,
                      ActivityLogger activity) {
        this.keyRepo     = keyRepo;
        this.projectRepo = projectRepo;
        this.mapper      = mapper;
        this.activity    = activity;
    }

    public PagedList<KeyDto> find(KeyCriteria c) {
        var query = c.projectId != null
            ? keyRepo.find("project.id = ?1 ORDER BY name", c.projectId)
            : keyRepo.findAll();
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public KeyDto get(UUID id) {
        return mapper.toDto(keyRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
    }

    public KeyDto getByOwnerAndProjectNameAndName(String username, String projectName, String keyName) {
        var project = projectRepo.findByOwnerUsernameAndName(username, projectName)
                .orElseThrow(NotFoundException::new);
        return mapper.toDto(
                keyRepo.findByProjectAndName(project.id, keyName)
                       .orElseThrow(NotFoundException::new)
        );
    }

    @Transactional
    public KeyDto create(KeyDto dto) {
        var project = projectRepo.findByIdOptional(dto.projectId)
                .orElseThrow(NotFoundException::new);
        Key k = new Key(project, dto.name);
        keyRepo.persist(k);
        KeyDto after = mapper.toDto(k);
        activity.publish(ActionType.Create, project, KeyDto.class, null, after);
        return after;
    }

    @Transactional
    public KeyDto update(KeyDto dto) {
        Key k = keyRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        KeyDto before = mapper.toDto(k);
        if (dto.name != null) k.name = dto.name;
        KeyDto after = mapper.toDto(k);
        activity.publish(ActionType.Update, k.project, KeyDto.class, before, after);
        return after;
    }

    @Transactional
    public KeyDto delete(UUID id) {
        Key k = keyRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        KeyDto before = mapper.toDto(k);
        var project = k.project;
        keyRepo.delete(k);
        activity.publish(ActionType.Delete, project, KeyDto.class, before, null);
        return before;
    }
}
