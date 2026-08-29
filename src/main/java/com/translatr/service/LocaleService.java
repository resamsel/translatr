package com.translatr.service;

import com.translatr.criteria.LocaleCriteria;
import com.translatr.dto.LocaleDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.ActionType;
import com.translatr.model.Locale;
import com.translatr.repository.LocaleRepository;
import com.translatr.repository.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LocaleService {

    private final LocaleRepository  localeRepo;
    private final ProjectRepository projectRepo;
    private final DtoMapper         mapper;
    private final ActivityLogger    activity;

    @Inject
    public LocaleService(LocaleRepository localeRepo, ProjectRepository projectRepo, DtoMapper mapper,
                         ActivityLogger activity) {
        this.localeRepo  = localeRepo;
        this.projectRepo = projectRepo;
        this.mapper      = mapper;
        this.activity    = activity;
    }

    private static final List<String> ORDERABLE = List.of("name", "whenCreated", "whenUpdated", "wordCount");

    public PagedList<LocaleDto> find(LocaleCriteria c) {
        // Port of LocaleRepositoryImpl.findBy: apply every populated criteria field, not just projectId.
        StringBuilder ql    = new StringBuilder("FROM Locale l WHERE 1 = 1");
        List<Object>  params = new ArrayList<>();

        if (c.projectId != null) {
            params.add(c.projectId);
            ql.append(" AND l.project.id = ?").append(params.size());
        }
        if (QuerySupport.hasText(c.localeName)) {
            params.add(c.localeName);
            ql.append(" AND l.name = ?").append(params.size());
        }
        if (QuerySupport.hasText(c.search)) {
            params.add(QuerySupport.like(c.search));
            ql.append(" AND lower(l.name) LIKE ?").append(params.size());
        }
        if (Boolean.TRUE.equals(c.missing)) {
            // locales that have no translation (optionally: no translation for a specific key)
            ql.append(" AND NOT EXISTS (SELECT 1 FROM Message m WHERE m.locale = l");
            if (c.keyId != null) {
                params.add(c.keyId);
                ql.append(" AND m.key.id = ?").append(params.size());
            }
            ql.append(')');
        }
        ql.append(' ').append(QuerySupport.orderBy(c.order, ORDERABLE, "ORDER BY name"));

        var query  = localeRepo.find(ql.toString(), params.toArray());
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public LocaleDto get(UUID id) {
        return mapper.toDto(localeRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
    }

    public LocaleDto getByOwnerAndProjectNameAndName(String username, String projectName, String localeName) {
        var project = projectRepo.findByOwnerUsernameAndName(username, projectName)
                .orElseThrow(NotFoundException::new);
        return mapper.toDto(
                localeRepo.findByProjectAndName(project.id, localeName)
                          .orElseThrow(NotFoundException::new)
        );
    }

    @Transactional
    public LocaleDto create(LocaleDto dto) {
        var project = projectRepo.findByIdOptional(dto.projectId)
                .orElseThrow(NotFoundException::new);
        Locale l = new Locale(project, dto.name);
        localeRepo.persist(l);
        LocaleDto after = mapper.toDto(l);
        activity.publish(ActionType.Create, project, LocaleDto.class, null, after);
        return after;
    }

    @Transactional
    public LocaleDto update(LocaleDto dto) {
        Locale l = localeRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        LocaleDto before = mapper.toDto(l);
        if (dto.name != null) l.name = dto.name;
        LocaleDto after = mapper.toDto(l);
        activity.publish(ActionType.Update, l.project, LocaleDto.class, before, after);
        return after;
    }

    @Transactional
    public LocaleDto delete(UUID id) {
        Locale l = localeRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        LocaleDto before = mapper.toDto(l);
        var project = l.project;
        localeRepo.delete(l);
        activity.publish(ActionType.Delete, project, LocaleDto.class, before, null);
        return before;
    }
}
