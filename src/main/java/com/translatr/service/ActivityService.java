package com.translatr.service;

import com.translatr.dto.ActivityDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.repository.LogEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ActivityService {

    @Inject LogEntryRepository logRepo;
    @Inject DtoMapper          mapper;

    public PagedList<ActivityDto> findByUser(UUID userId, int offset, int limit) {
        var list  = logRepo.findByUser(userId, offset / Math.max(limit,1), limit)
                           .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, list.size(), offset, limit);
    }

    public PagedList<ActivityDto> findByProject(UUID projectId, int offset, int limit) {
        var list  = logRepo.findByProject(projectId, offset / Math.max(limit,1), limit)
                           .stream().map(mapper::toDto).collect(Collectors.toList());
        return new PagedList<>(list, list.size(), offset, limit);
    }
}
