package com.translatr.service;

import com.translatr.dto.GlobalFeatureFlagDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.Feature;
import com.translatr.model.FeatureFlag;
import com.translatr.repository.FeatureFlagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** CRUD over the global {@link FeatureFlag} rows. Writes are admin-gated at the resource layer. */
@ApplicationScoped
public class GlobalFeatureFlagService {

    private final FeatureFlagRepository repo;
    private final DtoMapper             mapper;

    @Inject
    public GlobalFeatureFlagService(FeatureFlagRepository repo, DtoMapper mapper) {
        this.repo   = repo;
        this.mapper = mapper;
    }

    public List<GlobalFeatureFlagDto> list() {
        return repo.listAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public GlobalFeatureFlagDto set(String feature, boolean enabled) {
        if (Feature.of(feature).isEmpty()) {
            throw new BadRequestException("Unknown feature: " + feature);
        }
        FeatureFlag flag = repo.findByFeature(feature).orElse(null);
        if (flag == null) {
            flag = FeatureFlag.of(feature, enabled);
            repo.persist(flag);
        } else {
            flag.enabled = enabled;
        }
        return mapper.toDto(flag);
    }

    @Transactional
    public void delete(UUID id) {
        FeatureFlag flag = repo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        repo.delete(flag);
    }
}
