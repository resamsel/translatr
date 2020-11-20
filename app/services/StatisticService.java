package services;

import com.google.inject.ImplementedBy;
import dto.Statistic;
import play.mvc.Http;
import services.impl.StatisticServiceImpl;

@ImplementedBy(StatisticServiceImpl.class)
public interface StatisticService {

  Statistic find(Http.Request request);
}
