package com.translatr.controller;

import com.translatr.criteria.KeyCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KeyResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID projectId = UUID.randomUUID();
        UUID localeId  = UUID.randomUUID();

        KeyCriteria criteria = KeyResource.toCriteria(
                "needle", 5, 10, "name", "count", projectId, localeId, true);

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("name");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.projectId).isEqualTo(projectId);
        assertThat(criteria.localeId).isEqualTo(localeId);
        assertThat(criteria.missing).isTrue();
    }
}
