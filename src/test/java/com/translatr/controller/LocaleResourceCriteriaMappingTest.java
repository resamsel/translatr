package com.translatr.controller;

import com.translatr.criteria.LocaleCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LocaleResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID projectId = UUID.randomUUID();
        UUID keyId     = UUID.randomUUID();

        LocaleCriteria criteria = LocaleResource.toCriteria(
                "needle", 5, 10, "name", "count", projectId, keyId, true, "de");

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("name");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.projectId).isEqualTo(projectId);
        assertThat(criteria.keyId).isEqualTo(keyId);
        assertThat(criteria.missing).isTrue();
        assertThat(criteria.localeName).isEqualTo("de");
    }
}
