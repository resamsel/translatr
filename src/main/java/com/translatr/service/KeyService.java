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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class KeyService {

    private final KeyRepository     keyRepo;
    private final ProjectRepository projectRepo;
    private final DtoMapper         mapper;
    private final ActivityLogger    activity;
    private final ProgressService   progress;

    @Inject
    public KeyService(KeyRepository keyRepo, ProjectRepository projectRepo, DtoMapper mapper,
                      ActivityLogger activity, ProgressService progress) {
        this.keyRepo     = keyRepo;
        this.projectRepo = projectRepo;
        this.mapper      = mapper;
        this.activity    = activity;
        this.progress    = progress;
    }

    private static final List<String> ORDERABLE = List.of("name", "whenCreated", "whenUpdated", "wordCount");

    public PagedList<KeyDto> find(KeyCriteria c) {
        // Port of KeyRepositoryImpl.findBy: apply every populated criteria field, not just projectId.
        StringBuilder ql    = new StringBuilder("FROM Key k WHERE 1 = 1");
        List<Object>  params = new ArrayList<>();

        if (c.projectId != null) {
            params.add(c.projectId);
            ql.append(" AND k.project.id = ?").append(params.size());
        }
        if (QuerySupport.hasText(c.search)) {
            params.add(QuerySupport.like(c.search));
            ql.append(" AND lower(k.name) LIKE ?").append(params.size());
        }
        if (Boolean.TRUE.equals(c.missing)) {
            // keys that have no translation (optionally: no translation in a specific locale)
            ql.append(" AND NOT EXISTS (SELECT 1 FROM Message m WHERE m.key = k");
            if (c.localeId != null) {
                params.add(c.localeId);
                ql.append(" AND m.locale.id = ?").append(params.size());
            }
            ql.append(')');
        }
        ql.append(' ').append(QuerySupport.orderBy(c.order, ORDERABLE, "ORDER BY name"));

        var query  = keyRepo.find(ql.toString(), params.toArray());
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        if (QuerySupport.wants(c.fetch, "progress") && c.projectId != null && !list.isEmpty()) {
            var byKey = progress.keyProgress(c.projectId);
            list.forEach(d -> d.progress = byKey.getOrDefault(d.id, 0.0));
        }
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
