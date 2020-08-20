package controllers;

import org.apache.commons.io.IOUtils;
import play.api.http.HttpErrorHandler;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 19 Jan 2017
 */
@Singleton
public class ApiDocs extends Assets {
  /**
   * @param errorHandler
   * @param meta
   */
  @Inject
  public ApiDocs(HttpErrorHandler errorHandler, AssetsMetadata meta) {
    super(errorHandler, meta);
  }

  public CompletionStage<Result> index() {
    return CompletableFuture
        .supplyAsync(() -> getClass().getResourceAsStream("/public/lib/swagger-ui/index.html"))
        .thenApply(stream -> Results.ok(postProcessIndex(stream)).as("text/html"));
  }

  private String postProcessIndex(InputStream stream) {
    try {
      return IOUtils.toString(stream, StandardCharsets.UTF_8)
          .replaceFirst("http://petstore.swagger.io/v2/swagger.json", "/api/swagger.json")
          .replaceFirst("css/screen.css", "../assets/stylesheets/theme-material.css");
    } catch (IOException e) {
      return null;
    }
  }
}
