package com.translatr.controller;

import com.translatr.dto.StatisticsDto;
import com.translatr.service.StatisticsService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource {

    private final StatisticsService statisticsService;

    @Inject
    public StatisticsResource(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GET
    @Path("/statistics")
    @PermitAll
    public StatisticsDto find() {
        return statisticsService.find();
    }
}
