package com.translatr.service;

import com.translatr.criteria.FeatureFlagCriteria;
import com.translatr.dto.FeatureFlagDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.UserFeatureFlag;
import com.translatr.repository.UserFeatureFlagRepository;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class FeatureFlagService {

    private final UserFeatureFlagRepository featureFlagRepo;
    private final UserRepository            userRepo;
    private final DtoMapper                 mapper;

    @Inject
    public FeatureFlagService(UserFeatureFlagRepository featureFlagRepo, UserRepository userRepo, DtoMapper mapper) {
        this.featureFlagRepo = featureFlagRepo;
        this.userRepo        = userRepo;
        this.mapper          = mapper;
    }

    public PagedList<FeatureFlagDto> find(FeatureFlagCriteria c, UUID currentUserId) {
        var query = featureFlagRepo.find(
                "user.id = ?1 ORDER BY whenCreated DESC", currentUserId);
        long total = query.count();
        var list = query.page(c.offset / Math.max(c.limit, 1), c.limit).list()
                .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public FeatureFlagDto get(UUID id) {
        return mapper.toDto(featureFlagRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
    }

    @Transactional
    public FeatureFlagDto create(FeatureFlagDto dto) {
        var user = userRepo.findByIdOptional(dto.userId).orElseThrow(NotFoundException::new);
        var flag = UserFeatureFlag.of(user, dto.feature, dto.enabled);
        featureFlagRepo.persist(flag);
        return mapper.toDto(flag);
    }

    @Transactional
    public FeatureFlagDto update(FeatureFlagDto dto) {
        var flag = featureFlagRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        flag.enabled = dto.enabled;
        if (dto.feature != null) flag.feature = dto.feature;
        return mapper.toDto(flag);
    }

    @Transactional
    public FeatureFlagDto delete(UUID id) {
        var flag = featureFlagRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        featureFlagRepo.delete(flag);
        return mapper.toDto(flag);
    }
}
