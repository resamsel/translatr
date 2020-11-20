package services.api;

import com.google.inject.ImplementedBy;
import dto.Statistic;
import play.mvc.Http;
import services.api.impl.StatisticApiServiceImpl;

@ImplementedBy(StatisticApiServiceImpl.class)
public interface StatisticApiService {
  Statistic find(Http.Request request);
}
