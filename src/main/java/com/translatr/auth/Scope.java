package com.translatr.auth;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Access-token scopes. Wire strings mirror {@code ui/libs/translatr-model/src/lib/model/scope.ts};
 * {@code access_token.scope} stores them comma-separated.
 */
public enum Scope {
    UserRead("read:user"),
    UserWrite("write:user"),
    AccessTokenRead("read:accesstoken"),
    AccessTokenWrite("write:accesstoken"),
    ProjectRead("read:project"),
    ProjectWrite("write:project"),
    LocaleRead("read:locale"),
    LocaleWrite("write:locale"),
    KeyRead("read:key"),
    KeyWrite("write:key"),
    MessageRead("read:message"),
    MessageWrite("write:message"),
    MemberRead("read:member"),
    MemberWrite("write:member"),
    NotificationRead("read:notification"),
    NotificationWrite("write:notification"),
    FeatureFlagRead("read:featureflag"),
    FeatureFlagWrite("write:featureflag");

    private final String value;

    Scope(String value) { this.value = value; }

    /** The wire string, e.g. {@code read:project}. */
    public String value() { return value; }

    /** Looks up a scope by wire string (trimmed); empty for null or unknown values. */
    public static Optional<Scope> fromString(String s) {
        if (s == null) return Optional.empty();
        String trimmed = s.trim();
        return Arrays.stream(values()).filter(v -> v.value.equals(trimmed)).findFirst();
    }

    /** Parses a comma-separated scope column, dropping blanks and unknown values. */
    public static Set<Scope> parse(String commaSeparated) {
        EnumSet<Scope> result = EnumSet.noneOf(Scope.class);
        if (commaSeparated == null) return result;
        for (String part : commaSeparated.split(","))
            fromString(part).ifPresent(result::add);
        return result;
    }
}
