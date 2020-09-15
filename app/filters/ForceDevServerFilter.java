package filters;

import akka.stream.Materializer;
import com.typesafe.config.Config;
import play.Environment;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static play.mvc.Results.redirect;
import static utils.ConfigKey.RedirectBase;

public class ForceDevServerFilter extends Filter {
  private final Config configuration;
  private final Environment environment;

  @Inject
  public ForceDevServerFilter(Materializer mat, Config configuration, Environment environment) {
    super(mat);
    this.configuration = configuration;
    this.environment = environment;
  }

  @Override
  public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> next, Http.RequestHeader rh) {
    if (environment.isDev() && (rh.path().startsWith("/ui") || rh.path().startsWith("/admin"))) {
      String redirectBase = RedirectBase.getOrDefault(configuration, "");
      if (!redirectBase.equals("") && !hasRedirectBase(rh, redirectBase)) {
        return CompletableFuture.completedFuture(redirect(redirectBase + rh.path()));
      }
    }
    return next.apply(rh);
  }

  private boolean hasRedirectBase(Http.RequestHeader rh, String redirectBase) {
    String protocol = rh.secure() ? "https" : "http";
    String uri = protocol + "://" + rh.host() + rh.uri();
    return uri.startsWith(redirectBase);
  }
}
