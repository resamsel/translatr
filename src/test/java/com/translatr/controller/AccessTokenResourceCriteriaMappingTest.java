package com.translatr.controller;

import com.translatr.criteria.AccessTokenCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID userId = UUID.randomUUID();

        AccessTokenCriteria criteria =
                AccessTokenResource.toCriteria("needle", 5, 10, "name", "count", userId);

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("name");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.userId).isEqualTo(userId);
    }
}
