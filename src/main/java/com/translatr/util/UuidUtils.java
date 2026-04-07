package com.translatr.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UUID parsing utilities (ported from the Play-era JsonUtils).
 */
public final class UuidUtils {

    private UuidUtils() {}

    /** Parses a UUID string; returns {@code null} on blank or invalid input. */
    public static UUID getUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuid.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parses a comma-separated list of UUID strings.
     * Returns {@code null} on blank input; invalid UUIDs are silently skipped.
     */
    public static List<UUID> getUuids(String uuids) {
        if (uuids == null || uuids.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(uuids.split(","))
                .map(UuidUtils::getUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

