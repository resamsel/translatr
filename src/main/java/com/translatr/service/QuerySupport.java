package com.translatr.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Helpers for turning a {@link com.translatr.criteria.SearchCriteria} into a Panache query.
 *
 * <p>Every {@code *Service.find(...)} must translate the <em>whole</em> criteria into its query:
 * each populated field narrows the result (combined with {@code AND}), {@code ?search=} maps to an
 * entity-specific {@code LIKE} disjunction and {@code ?order=} maps to a safe {@code ORDER BY}.
 * Dropping any of them lets a request such as the key editor's
 * {@code ?projectId=..&keyName=..&localeIds=..} fall through to "page 0 of everything", which is how
 * the key editor ended up showing a different key's translations.
 */
final class QuerySupport {

    private QuerySupport() {
    }

    /** Split a comma-separated list of UUIDs, as the UI sends {@code localeIds} / {@code keyIds}. */
    static List<UUID> uuidCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .map(UUID::fromString)
                     .collect(Collectors.toList());
    }

    static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Whether the {@code ?fetch=} query param opts into a given association/expansion. {@code fetch}
     * is a comma-separated list (e.g. {@code count,progress}); matching is exact per token, so
     * {@code featureFlags} never satisfies a check for {@code features}.
     */
    static boolean wants(String fetch, String token) {
        if (fetch == null) {
            return false;
        }
        return Arrays.stream(fetch.split(","))
                     .map(String::trim)
                     .anyMatch(token::equals);
    }

    /** {@code %value%} for a case-insensitive {@code LIKE}, or {@code null} when there is nothing to match. */
    static String like(String search) {
        return hasText(search) ? "%" + search.toLowerCase() + "%" : null;
    }

    /**
     * Build a safe {@code ORDER BY} clause from the {@code ?order=} query param. Only
     * {@code <field> [asc|desc]} where {@code field} is white-listed is honoured; anything else
     * (unknown field, injection attempt, blank) falls back to {@code fallback}.
     */
    static String orderBy(String order, List<String> allowedFields, String fallback) {
        if (!hasText(order)) {
            return fallback;
        }
        String[] parts = order.trim().split("\\s+");
        if (!allowedFields.contains(parts[0])) {
            return fallback;
        }
        String dir = parts.length > 1 && parts[1].equalsIgnoreCase("desc") ? "DESC" : "ASC";
        return "ORDER BY " + parts[0] + " " + dir;
    }
}
