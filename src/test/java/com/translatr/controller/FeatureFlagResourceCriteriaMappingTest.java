package com.translatr.controller;

import com.translatr.criteria.FeatureFlagCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureFlagResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID userId = UUID.randomUUID();

        FeatureFlagCriteria criteria = FeatureFlagResource.toCriteria(
                "needle", 5, 10, "createdAt desc", "count", userId, "dark-mode");

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("createdAt desc");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.userId).isEqualTo(userId);
        assertThat(criteria.feature).isEqualTo("dark-mode");
    }
}
