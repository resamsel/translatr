package com.translatr.controller;

import com.translatr.criteria.UserCriteria;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UserCriteria criteria = UserResource.toCriteria(
                "needle", 5, 10, "email desc", "count", "jane.doe", "jane@example.com");

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("email desc");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.username).isEqualTo("jane.doe");
        assertThat(criteria.email).isEqualTo("jane@example.com");
    }
}
