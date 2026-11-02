package com.translatr.auth;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeTest {

    @Test
    void fromString_known() {
        assertThat(Scope.fromString("write:project")).contains(Scope.ProjectWrite);
    }

    @Test
    void fromString_trims() {
        assertThat(Scope.fromString("  read:key ")).contains(Scope.KeyRead);
    }

    @Test
    void fromString_unknown() {
        assertThat(Scope.fromString("admin:all")).isEmpty();
        assertThat(Scope.fromString(null)).isEmpty();
    }

    @Test
    void parse_messy() {
        assertThat(Scope.parse("read:project, write:key ,,bogus"))
                .containsExactlyInAnyOrder(Scope.ProjectRead, Scope.KeyWrite);
    }

    @Test
    void parse_nullAndBlank() {
        assertThat(Scope.parse(null)).isEmpty();
        assertThat(Scope.parse("  ")).isEmpty();
    }

    @Test
    void wireStringsMatchUi() {
        assertThat(Arrays.stream(Scope.values()).map(Scope::value)).containsExactly(
                "read:user", "write:user", "read:accesstoken", "write:accesstoken",
                "read:project", "write:project", "read:locale", "write:locale",
                "read:key", "write:key", "read:message", "write:message",
                "read:member", "write:member", "read:notification", "write:notification",
                "read:featureflag", "write:featureflag");
    }
}
