package com.translatr.controller;

import com.translatr.dto.StatisticsDto;
import com.translatr.generated.api.StatisticsApi;
import com.translatr.service.StatisticsService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;

public class StatisticsResource implements StatisticsApi {

    private final StatisticsService statisticsService;

    @Inject
    public StatisticsResource(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    @PermitAll
    public StatisticsDto getStatistics() {
        return statisticsService.find();
    }
}
