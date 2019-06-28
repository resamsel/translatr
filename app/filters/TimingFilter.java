package filters;

import akka.stream.Materializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Filter;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import utils.Stopwatch;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 *
 * @author resamsel
 * @version 13 Jul 2016
 */
public class TimingFilter extends Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(TimingFilter.class);

  /**
   * @param mat
   */
  @Inject
  public TimingFilter(Materializer mat) {
    super(mat);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CompletionStage<Result> apply(Function<RequestHeader, CompletionStage<Result>> next,
      RequestHeader rh) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    return next.apply(rh).thenApply(result -> {
      if (!rh.uri().startsWith("/assets/")) {
        LOGGER.info("{} {} took {} and returned {}", rh.method(), rh.uri(), stopwatch,
            result.status());

        return result.withHeader("X-Timing", stopwatch.toString());
      }

      return result;
    });
  }
}
