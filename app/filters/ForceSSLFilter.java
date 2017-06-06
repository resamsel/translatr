package filters;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import akka.stream.Materializer;
import play.Configuration;
import play.api.Play;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import utils.ConfigKey;

/**
 * @author resamsel
 * @version 19 May 2017
 */
public class ForceSSLFilter extends Filter {
  private final Configuration configuration;

  @Inject
  public ForceSSLFilter(Materializer mat, Configuration configuration) {
    super(mat);
    this.configuration = configuration;
  }

  @Override
  public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> next,
      Http.RequestHeader rh) {
    if (Play.current().isProd() && ConfigKey.ForceSSL.getBoolean(configuration)) {
      String[] httpsHeader =
          rh.headers().getOrDefault(Http.HeaderNames.X_FORWARDED_PROTO, new String[] {"http"});
      if (StringUtils.isEmpty(httpsHeader[0]) || httpsHeader[0].equalsIgnoreCase("http"))
        return CompletableFuture.completedFuture(
            Results.movedPermanently(String.format("https://%s%s", rh.host(), rh.uri())));
      else
        return next.apply(rh).thenApply(
            result -> result.withHeader("Strict-Transport-Security", "max-age=31536000"));
    }

    return next.apply(rh);
  }
}
