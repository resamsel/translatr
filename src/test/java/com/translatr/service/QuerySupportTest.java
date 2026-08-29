package com.translatr.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuerySupportTest {

    @Test
    void wants_isFalse_whenFetchIsNull() {
        assertThat(QuerySupport.wants(null, "progress")).isFalse();
    }

    @Test
    void wants_isTrue_forASingleTokenMatch() {
        assertThat(QuerySupport.wants("progress", "progress")).isTrue();
    }

    @Test
    void wants_isTrue_forOneTokenInACommaList() {
        assertThat(QuerySupport.wants("count,progress", "progress")).isTrue();
        assertThat(QuerySupport.wants("count,progress", "count")).isTrue();
    }

    @Test
    void wants_ignoresSurroundingWhitespace() {
        assertThat(QuerySupport.wants("count, progress", "progress")).isTrue();
    }

    @Test
    void wants_isFalse_whenTokenAbsent() {
        assertThat(QuerySupport.wants("count,progress", "members")).isFalse();
    }

    @Test
    void wants_doesNotMatchOnSubstring() {
        assertThat(QuerySupport.wants("featureFlags", "features")).isFalse();
    }
}
