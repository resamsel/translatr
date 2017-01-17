package controllers;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.LocaleCriteria;
import models.Locale;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import services.LocaleService;
import services.LogEntryService;
import services.UserService;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@With(ApiAction.class)
public class LocalesApi extends Api<Locale, UUID, LocaleCriteria, dto.Locale> {
  @Inject
  public LocalesApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, LocaleService localeService) {
    super(injector, cache, auth, userService, logEntryService, localeService, Locale::byId,
        Locale::findBy, dto.Locale.class, dto.Locale::from, Locale::from,
        new Scope[] {Scope.ProjectRead, Scope.LocaleRead},
        new Scope[] {Scope.ProjectRead, Scope.LocaleWrite});
  }

  public CompletionStage<Result> find(UUID projectId) {
    return findBy(
        new LocaleCriteria().withProjectId(projectId)
            .withLocaleName(request().getQueryString("localeName"))
            .withSearch(request().getQueryString("search")),
        criteria -> checkProjectRole(projectId, User.loggedInUser(), ProjectRole.Owner,
            ProjectRole.Translator, ProjectRole.Developer));
  }

  public CompletionStage<Result> upload(UUID localeId, String fileType) {
    return CompletableFuture.supplyAsync(() -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
          Scope.MessageWrite);

      return injector.instanceOf(Locales.class).importLocale(Locale.byId(localeId), request());
    }, executionContext.current())
        .thenApply(success -> ok(Json.newObject().put("status", success)));
  }

  public CompletionStage<Result> download(UUID localeId, String fileType) {
    return CompletableFuture.supplyAsync(() -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
          Scope.MessageRead);

      return injector.instanceOf(Locales.class).download(localeId, fileType);
    }, executionContext.current()).thenApply(data -> ok(new ByteArrayInputStream(data)))
        .exceptionally(Api::handleException);
  }
}
