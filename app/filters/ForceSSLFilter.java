package filters;

import akka.stream.Materializer;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import utils.ConfigKey;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author resamsel
 * @version 19 May 2017
 */
public class ForceSSLFilter extends Filter {
  private final Config configuration;

  @Inject
  public ForceSSLFilter(Materializer mat, Config configuration) {
    super(mat);
    this.configuration = configuration;
  }

  @Override
  public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> next,
                                       Http.RequestHeader rh) {
    if (ConfigKey.ForceSSL.getBoolean(configuration)) {
      String httpsHeader = rh.header(Http.HeaderNames.X_FORWARDED_PROTO).orElse("http");
      if (StringUtils.isEmpty(httpsHeader) || httpsHeader.equalsIgnoreCase("http"))
        return CompletableFuture.completedFuture(
                Results.movedPermanently(String.format("https://%s%s", rh.host(), rh.uri())));
      else
        return next.apply(rh).thenApply(
                result -> result.withHeader("Strict-Transport-Security", "max-age=31536000"));
    }

    return next.apply(rh);
  }
}
