package services.api.impl;

import dto.Statistic;
import play.mvc.Http;
import services.StatisticService;
import services.api.StatisticApiService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StatisticApiServiceImpl implements StatisticApiService {
  private final StatisticService statisticService;

  @Inject
  public StatisticApiServiceImpl(StatisticService statisticService) {
    this.statisticService = statisticService;
  }

  @Override
  public Statistic find(Http.Request request) {
    return statisticService.find(request);
  }
}
