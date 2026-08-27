package com.translatr.service;

import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.AccessToken;
import com.translatr.model.User;
import com.translatr.repository.AccessTokenRepository;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AccessTokenService {

    private final AccessTokenRepository tokenRepo;
    private final UserRepository        userRepo;
    private final DtoMapper             mapper;

    @Inject
    public AccessTokenService(AccessTokenRepository tokenRepo, UserRepository userRepo, DtoMapper mapper) {
        this.tokenRepo = tokenRepo;
        this.userRepo  = userRepo;
        this.mapper    = mapper;
    }

    public PagedList<AccessTokenDto> find(AccessTokenCriteria c, UUID currentUserId) {
        var query = tokenRepo.find("user.id = ?1 ORDER BY whenCreated DESC", currentUserId);
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public AccessTokenDto get(Long id) {
        return mapper.toDto(tokenRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
    }

    @Transactional
    public Optional<User> findUserByKey(String key) {
        return tokenRepo.findByKey(key).map(t -> t.user);
    }

    @Transactional
    public AccessTokenDto create(AccessTokenDto dto, User owner) {
        AccessToken t = new AccessToken();
        t.user  = owner;
        t.name  = dto.name;
        t.key   = java.util.UUID.randomUUID().toString().replace("-", "");
        t.scope = dto.scope;
        tokenRepo.persist(t);
        return mapper.toDto(t);
    }

    @Transactional
    public AccessTokenDto update(AccessTokenDto dto) {
        AccessToken t = tokenRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        if (dto.name  != null) t.name  = dto.name;
        if (dto.scope != null) t.scope = dto.scope;
        return mapper.toDto(t);
    }

    @Transactional
    public AccessTokenDto delete(Long id) {
        AccessToken t = tokenRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        tokenRepo.delete(t);
        return mapper.toDto(t);
    }
}
