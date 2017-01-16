package controllers;

import java.util.UUID;

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
public class LocalesApi extends Api<Locale, dto.Locale, UUID> {
  @Inject
  public LocalesApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, LocaleService localeService) {
    super(injector, cache, auth, userService, logEntryService, localeService, Locale::byId,
        dto.Locale.class, dto.Locale::from, Locale::from,
        new Scope[] {Scope.ProjectRead, Scope.LocaleRead},
        new Scope[] {Scope.ProjectRead, Scope.LocaleWrite});
  }

  public Result find(UUID projectId) {
    return projectCatch(projectId, project -> {
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Translator,
          ProjectRole.Developer);

      return toJsons(dtoMapper, () -> {
        return Locale.findBy(new LocaleCriteria().withProjectId(project.id)
            .withLocaleName(request().getQueryString("localeName")));
      });
    });
  }

  public Result upload(UUID localeId, String fileType) {
    return tryCatch(() -> locale(localeId, locale -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
          Scope.MessageWrite);

      injector.instanceOf(Locales.class).importLocale(locale, request());

      return ok(Json.newObject().put("status", "OK"));
    }));
  }

  public Result download(UUID localeId, String fileType) {
    return tryCatch(() -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
          Scope.MessageRead);

      return injector.instanceOf(Locales.class).download(localeId, fileType);
    });
  }
}
