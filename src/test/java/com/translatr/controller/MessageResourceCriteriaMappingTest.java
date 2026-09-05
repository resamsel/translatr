package com.translatr.controller;

import com.translatr.criteria.MessageCriteria;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageResourceCriteriaMappingTest {

    @Test
    void toCriteria_mapsEveryFlatParameterToTheMatchingCriteriaField() {
        UUID projectId = UUID.randomUUID();
        UUID localeId  = UUID.randomUUID();
        UUID keyId     = UUID.randomUUID();

        MessageCriteria criteria = MessageResource.toCriteria(
                "needle", 5, 10, "key.name", "count", projectId, localeId,
                "11111111-1111-1111-1111-111111111111,22222222-2222-2222-2222-222222222222",
                keyId,
                "33333333-3333-3333-3333-333333333333,44444444-4444-4444-4444-444444444444",
                "greeting");

        assertThat(criteria.search).isEqualTo("needle");
        assertThat(criteria.offset).isEqualTo(5);
        assertThat(criteria.limit).isEqualTo(10);
        assertThat(criteria.order).isEqualTo("key.name");
        assertThat(criteria.fetch).isEqualTo("count");
        assertThat(criteria.projectId).isEqualTo(projectId);
        assertThat(criteria.localeId).isEqualTo(localeId);
        assertThat(criteria.localeIds)
                .isEqualTo("11111111-1111-1111-1111-111111111111,22222222-2222-2222-2222-222222222222");
        assertThat(criteria.keyId).isEqualTo(keyId);
        assertThat(criteria.keyIds)
                .isEqualTo("33333333-3333-3333-3333-333333333333,44444444-4444-4444-4444-444444444444");
        assertThat(criteria.keyName).isEqualTo("greeting");
    }
}
