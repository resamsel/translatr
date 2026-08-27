package com.translatr.service;

import com.translatr.dto.ActivityDto;
import com.translatr.dto.AggregateDto;
import com.translatr.dto.PagedList;
import com.translatr.mapper.DtoMapper;
import com.translatr.repository.LogEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ActivityService {

    private final LogEntryRepository logRepo;
    private final DtoMapper          mapper;
    private final EntityManager      em;

    @Inject
    public ActivityService(LogEntryRepository logRepo, DtoMapper mapper, EntityManager em) {
        this.logRepo = logRepo;
        this.mapper  = mapper;
        this.em      = em;
    }

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

    /**
     * Returns daily activity counts from the log_entry table.
     * Optionally filtered by projectId and/or userId.
     * This is a public endpoint — no auth required.
     */
    @SuppressWarnings("unchecked")
    public PagedList<AggregateDto> getAggregates(UUID projectId, UUID userId, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT when_created::date AS date, count(*) AS cnt" +
                " FROM log_entry WHERE 1=1");

        List<Object> params = new ArrayList<>();
        int paramIdx = 1;
        if (projectId != null) {
            sql.append(" AND project_id = ?").append(paramIdx++);
            params.add(projectId);
        }
        if (userId != null) {
            sql.append(" AND user_id = ?").append(paramIdx++);
            params.add(userId);
        }
        sql.append(" GROUP BY 1 ORDER BY 1");

        var countQuery = em.createNativeQuery(
                "SELECT count(*) FROM (" + sql + ") sub");
        // Native queries can't mix JDBC-style "?" with ordinal "?N" placeholders in the same
        // query, so LIMIT/OFFSET use ordinal params too, continuing on from the WHERE params.
        var dataQuery  = em.createNativeQuery(
                sql + " LIMIT ?" + paramIdx + " OFFSET ?" + (paramIdx + 1));

        // bind WHERE params
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
            dataQuery.setParameter(i + 1, params.get(i));
        }
        dataQuery.setParameter(params.size() + 1, limit);
        dataQuery.setParameter(params.size() + 2, offset);

        long total = ((Number) countQuery.getSingleResult()).longValue();

        List<Object[]> rows = dataQuery.getResultList();
        List<AggregateDto> list = rows.stream().map(row -> {
            AggregateDto dto = new AggregateDto();
            // Hibernate 6 / the Pg driver hands back java.time.LocalDate for a `::date` column,
            // but older drivers/paths yield java.sql.Date — accept either.
            dto.date   = row[0] instanceof LocalDate ld ? ld : ((java.sql.Date) row[0]).toLocalDate();
            dto.millis = dto.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            dto.value  = ((Number) row[1]).intValue();
            return dto;
        }).collect(Collectors.toList());

        return new PagedList<>(list, total, offset, limit);
    }
}
