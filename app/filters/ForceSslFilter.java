package filters;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import akka.stream.Materializer;
import play.api.Play;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

/**
 * @author resamsel
 * @version 19 May 2017
 */
public class ForceSslFilter extends Filter {
  @Inject
  public ForceSslFilter(Materializer mat) {
    super(mat);
  }

  @Override
  public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> next,
      Http.RequestHeader rh) {
    if (Play.current().isProd()) {
      String[] httpsHeader =
          rh.headers().getOrDefault(Http.HeaderNames.X_FORWARDED_PROTO, new String[] {"http"});
      if (StringUtils.isEmpty(httpsHeader[0]) || httpsHeader[0].equalsIgnoreCase("http"))
        return CompletableFuture.completedFuture(
            Results.movedPermanently(String.format("https://%s%s", rh.host(), rh.uri())));
      else
        next.apply(rh).thenApply(
            result -> result.withHeader("Strict-Transport-Security", "max-age=31536000"));
    }

    return next.apply(rh);
  }
}
