package com.translatr.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PagedListTest {

    @Test
    void hasNext_isTrue_whenMoreRowsFollowTheWindow() {
        var page = new PagedList<>(List.of("a", "b"), 10, 0, 2);

        assertThat(page.hasNext).isTrue();
        assertThat(page.hasPrev).isFalse();
    }

    @Test
    void hasNext_isFalse_onTheLastPage() {
        var page = new PagedList<>(List.of("i", "j"), 10, 8, 2);

        assertThat(page.hasNext).isFalse();
        assertThat(page.hasPrev).isTrue();
    }

    @Test
    void hasNext_isFalse_whenTheWindowHoldsEverything() {
        var page = new PagedList<>(List.of("a", "b", "c"), 3, 0, 20);

        assertThat(page.hasNext).isFalse();
        assertThat(page.hasPrev).isFalse();
    }

    @Test
    void hasPrev_isTrue_forAnyNonZeroOffset() {
        var page = new PagedList<>(List.of("c"), 3, 2, 1);

        assertThat(page.hasPrev).isTrue();
        assertThat(page.hasNext).isFalse();
    }
}
