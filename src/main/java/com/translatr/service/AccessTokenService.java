package com.translatr.service;

import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.AccessToken;
import com.translatr.model.ActionType;
import com.translatr.model.User;
import com.translatr.repository.AccessTokenRepository;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class AccessTokenService {

    private final AccessTokenRepository tokenRepo;
    private final UserRepository        userRepo;
    private final DtoMapper             mapper;
    private final ActivityLogger        activity;

    @Inject
    public AccessTokenService(AccessTokenRepository tokenRepo, UserRepository userRepo, DtoMapper mapper,
                              ActivityLogger activity) {
        this.tokenRepo = tokenRepo;
        this.userRepo  = userRepo;
        this.mapper    = mapper;
        this.activity  = activity;
    }

    private static final List<String> ORDERABLE =
        List.of("name", "whenCreated", "whenUpdated", "scope");

    public PagedList<AccessTokenDto> find(AccessTokenCriteria c, UUID currentUserId) {
        // The token list is always scoped to the authenticated user (currentUserId is authoritative;
        // criteria.userId is never trusted here). On top of that honour ?search= and ?order=.
        StringBuilder ql    = new StringBuilder("user.id = ?1");
        List<Object>  params = new ArrayList<>();
        params.add(currentUserId);

        if (QuerySupport.hasText(c.search)) {
            params.add(QuerySupport.like(c.search));
            ql.append(" AND lower(name) LIKE ?").append(params.size());
        }
        ql.append(' ').append(QuerySupport.orderBy(c.order, ORDERABLE, "ORDER BY whenCreated DESC"));

        var query  = tokenRepo.find(ql.toString(), params.toArray());
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
        AccessTokenDto after = mapper.toDto(t);
        activity.publish(ActionType.Create, null, AccessTokenDto.class, null, after);
        return after;
    }

    @Transactional
    public AccessTokenDto update(AccessTokenDto dto) {
        AccessToken t = tokenRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        AccessTokenDto before = mapper.toDto(t);
        if (dto.name  != null) t.name  = dto.name;
        if (dto.scope != null) t.scope = dto.scope;
        AccessTokenDto after = mapper.toDto(t);
        activity.publish(ActionType.Update, null, AccessTokenDto.class, before, after);
        return after;
    }

    @Transactional
    public AccessTokenDto delete(Long id) {
        AccessToken t = tokenRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        tokenRepo.delete(t);
        return mapper.toDto(t);
    }
}
