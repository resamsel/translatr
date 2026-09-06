package com.translatr.controller;

import com.translatr.criteria.MemberCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MemberResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID projectId = UUID.randomUUID();
        UUID userId    = UUID.randomUUID();

        MemberCriteria criteria = MemberResource.toCriteria(
                "needle", 5, 10, "role", "count", projectId, userId);

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("role");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.projectId).isEqualTo(projectId);
        assertThat(criteria.userId).isEqualTo(userId);
    }
}
