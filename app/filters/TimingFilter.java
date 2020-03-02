package filters;

import akka.stream.Materializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Filter;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import services.MetricService;
import utils.Stopwatch;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author resamsel
 * @version 13 Jul 2016
 */
public class TimingFilter extends Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(TimingFilter.class);
  private final MetricService metricService;

  @Inject
  public TimingFilter(Materializer mat, MetricService metricService) {
    super(mat);
    this.metricService = metricService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CompletionStage<Result> apply(
          Function<RequestHeader, CompletionStage<Result>> next, RequestHeader rh) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    return metricService.time(() -> next.apply(rh).thenApply(result -> {
      metricService.consumeRequest(rh.method(), result.status());

      if (!rh.uri().startsWith("/assets/")) {
        LOGGER.info("{} {} took {} and returned {}", rh.method(), rh.uri(), stopwatch,
                result.status());

        return result.withHeader("X-Timing", stopwatch.toString());
      }

      return result;
    }));
  }
}
