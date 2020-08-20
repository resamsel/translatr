package utils;

import com.typesafe.config.Config;
import play.Environment;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class HttpErrorHandler extends DefaultHttpErrorHandler {

  @Inject
  public HttpErrorHandler(Config configuration, Environment environment,
                          OptionalSourceMapper sourceMapper, Provider<Router> routes) {
    super(configuration, environment, sourceMapper, routes);
  }

  private static boolean isContentTypeJson(Http.RequestHeader request) {
    Optional<String> contentType = request.contentType();
    return contentType.isPresent() && contentType.get().equals("application/json");
  }

  @Override
  protected CompletionStage<Result> onBadRequest(Http.RequestHeader request, String message) {
    if (isContentTypeJson(request)) {
      return CompletableFuture.completedFuture(Results.badRequest(ErrorUtils.toJson(message)));
    }

    return super.onBadRequest(request, message);
  }

  @Override
  protected CompletionStage<Result> onNotFound(Http.RequestHeader request, String message) {
    if (isContentTypeJson(request)) {
      return CompletableFuture.completedFuture(Results.notFound(ErrorUtils.toJson(message)));
    }

    return super.onNotFound(request, message);
  }

  @Override
  protected CompletionStage<Result> onForbidden(Http.RequestHeader request, String message) {
    if (isContentTypeJson(request)) {
      return CompletableFuture.completedFuture(Results.forbidden(ErrorUtils.toJson(message)));
    }

    return super.onForbidden(request, message);
  }

  @Override
  protected CompletionStage<Result> onDevServerError(Http.RequestHeader request, UsefulException exception) {
    if (isContentTypeJson(request)) {
      return CompletableFuture.completedFuture(Results.internalServerError(ErrorUtils.toJson(exception)));
    }

    return super.onDevServerError(request, exception);
  }

  @Override
  protected CompletionStage<Result> onProdServerError(Http.RequestHeader request, UsefulException exception) {
    if (isContentTypeJson(request)) {
      return CompletableFuture.completedFuture(Results.internalServerError(ErrorUtils.toJson(exception)));
    }

    return super.onProdServerError(request, exception);
  }
}
