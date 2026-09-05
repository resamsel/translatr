package com.translatr.controller;

import com.translatr.criteria.ProjectCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID ownerId  = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        ProjectCriteria criteria = ProjectResource.toCriteria(
                "needle", 5, 10, "name", "members", ownerId, "someowner", memberId, "some-project");

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("name");
        assertThat(criteria.fetch).isEqualTo("members");
        assertThat(criteria.ownerId).isEqualTo(ownerId);
        assertThat(criteria.ownerUsername).isEqualTo("someowner");
        assertThat(criteria.memberId).isEqualTo(memberId);
        assertThat(criteria.name).isEqualTo("some-project");
    }
}
