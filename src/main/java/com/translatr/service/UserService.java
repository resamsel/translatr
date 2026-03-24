package com.translatr.service;

import com.translatr.criteria.UserCriteria;
import com.translatr.dto.PagedList;
import com.translatr.dto.UserDto;
import com.translatr.mapper.DtoMapper;
import com.translatr.model.User;
import com.translatr.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject UserRepository userRepo;
    @Inject DtoMapper      mapper;

    public PagedList<UserDto> find(UserCriteria c) {
        var query = userRepo.findAll();
        long total = query.count();
        var list   = query.page(c.offset / Math.max(c.limit,1), c.limit).list()
                          .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, total, c.offset, c.limit);
    }

    public UserDto get(UUID id) {
        return mapper.toDto(userRepo.findByIdOptional(id).orElseThrow(NotFoundException::new));
    }

    public UserDto getByUsername(String username) {
        return mapper.toDto(userRepo.findByUsername(username).orElseThrow(NotFoundException::new));
    }

    @Transactional
    public UserDto update(UserDto dto) {
        User u = userRepo.findByIdOptional(dto.id).orElseThrow(NotFoundException::new);
        if (dto.name  != null) u.name  = dto.name;
        if (dto.email != null) u.email = dto.email;
        if (dto.settings != null) {
            if (u.settings == null) u.settings = new java.util.HashMap<>();
            u.settings.putAll(dto.settings);
        }
        return mapper.toDto(u);
    }

    @Transactional
    public UserDto delete(UUID id) {
        User u = userRepo.findByIdOptional(id).orElseThrow(NotFoundException::new);
        userRepo.delete(u);
        return mapper.toDto(u);
    }

    @Transactional
    public User findOrCreate(String providerKey, String providerUserId, String name, String email) {
        return userRepo.findByLinkedAccount(providerKey, providerUserId).orElseGet(() -> {
            User u = new User();
            u.name     = name;
            u.email    = email;
            u.username = email != null ? email.replaceAll("[^a-zA-Z0-9_.-]", "") : providerUserId;
            userRepo.persist(u);
            return u;
        });
    }
}
