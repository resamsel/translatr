package com.translatr.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Translation-progress aggregates, opt-in via {@code ?fetch=progress}. Ports the
 * {@code *RepositoryImpl.progress()} formulas from the Play implementation: progress is the
 * fraction of translatable cells that have a message.
 *
 * <ul>
 *   <li>locale: {@code messages(locale) / max(keys(project), 1)}</li>
 *   <li>key:    {@code messages(key)    / max(locales(project), 1)}</li>
 *   <li>project:{@code messages(project) / max(locales(project) * keys(project), 1)}</li>
 * </ul>
 *
 * <p>Each aggregate is a handful of {@code GROUP BY} queries over a single table, so no
 * {@code distinct} is needed (unlike the old cross-joined raw SQL). Rows with no messages never
 * appear in the returned map; callers substitute {@code 0.0}.
 */
@ApplicationScoped
public class ProgressService {

    private final EntityManager em;

    @Inject
    public ProgressService(EntityManager em) {
        this.em = em;
    }

    /** locale id &rarr; fraction of the project's keys translated in that locale. */
    public Map<UUID, Double> localeProgress(UUID projectId) {
        long keys = count("SELECT count(k.id) FROM Key k WHERE k.project.id = ?1", projectId);
        double denominator = Math.max(keys, 1);

        Map<UUID, Double> out = new HashMap<>();
        for (Object[] row : this.<Object[]>rows(
                "SELECT m.locale.id, count(m.id) FROM Message m "
                        + "WHERE m.locale.project.id = ?1 GROUP BY m.locale.id", projectId)) {
            out.put((UUID) row[0], ((Number) row[1]).doubleValue() / denominator);
        }
        return out;
    }

    /** key id &rarr; fraction of the project's locales in which that key is translated. */
    public Map<UUID, Double> keyProgress(UUID projectId) {
        long locales = count("SELECT count(l.id) FROM Locale l WHERE l.project.id = ?1", projectId);
        double denominator = Math.max(locales, 1);

        Map<UUID, Double> out = new HashMap<>();
        for (Object[] row : this.<Object[]>rows(
                "SELECT m.key.id, count(m.id) FROM Message m "
                        + "WHERE m.key.project.id = ?1 GROUP BY m.key.id", projectId)) {
            out.put((UUID) row[0], ((Number) row[1]).doubleValue() / denominator);
        }
        return out;
    }

    /** project id &rarr; fraction of the project's (locales &times; keys) cells that have a message. */
    public Map<UUID, Double> projectProgress(Collection<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = List.copyOf(projectIds);

        Map<UUID, Long> locales  = countByProject(
                "SELECT l.project.id, count(l.id) FROM Locale l WHERE l.project.id IN ?1 GROUP BY l.project.id", ids);
        Map<UUID, Long> keys     = countByProject(
                "SELECT k.project.id, count(k.id) FROM Key k WHERE k.project.id IN ?1 GROUP BY k.project.id", ids);
        Map<UUID, Long> messages = countByProject(
                "SELECT m.key.project.id, count(m.id) FROM Message m WHERE m.key.project.id IN ?1 GROUP BY m.key.project.id", ids);

        Map<UUID, Double> out = new HashMap<>();
        for (UUID id : ids) {
            double denominator = Math.max(locales.getOrDefault(id, 0L) * keys.getOrDefault(id, 0L), 1L);
            out.put(id, messages.getOrDefault(id, 0L) / denominator);
        }
        return out;
    }

    private long count(String jpql, UUID projectId) {
        return em.createQuery(jpql, Long.class).setParameter(1, projectId).getSingleResult();
    }

    private Map<UUID, Long> countByProject(String jpql, List<UUID> ids) {
        Map<UUID, Long> out = new HashMap<>();
        for (Object[] row : this.<Object[]>rows(jpql, ids)) {
            out.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> rows(String jpql, Object param) {
        return em.createQuery(jpql).setParameter(1, param).getResultList();
    }
}
