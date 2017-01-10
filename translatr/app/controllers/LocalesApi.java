package controllers;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
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
import play.mvc.BodyParser;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.With;
import services.LocaleService;
import services.LogEntryService;
import services.UserService;
import utils.JsonUtils;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@With(ApiAction.class)
public class LocalesApi extends Api<Locale, dto.Locale, UUID> {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalesApi.class);

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   * @param logEntryService
   * @param localeService
   * @param keyService
   * @param messageService
   */
  @Inject
  public LocalesApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, LocaleService localeService) {
    super(injector, cache, auth, userService, logEntryService, localeService);
  }

  public Result find(UUID projectId) {
    return project(projectId, project -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead);
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Translator,
          ProjectRole.Developer);

      return toJsons(dtoMapper(), () -> {
        return Locale.findBy(new LocaleCriteria().withProjectId(project.id)
            .withLocaleName(request().getQueryString("localeName")));
      });
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Supplier<Locale> creator(Request request) {
    JsonNode json = request().body().asJson();

    return project(JsonUtils.getUuid(json, "projectId"), project -> {
      checkPermissionAll("Access token not allowed", scopesCreate());
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Translator);

      return () -> service.create(json);
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result update() {
    return loggedInUser(user -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleWrite);

      dto.Locale json = Json.fromJson(request().body().asJson(), dto.Locale.class);
      if (json.id == null)
        throw new IllegalArgumentException("Field 'id' required");

      return locale(json.id, locale -> {
        checkProjectRole(locale.project, user, ProjectRole.Owner, ProjectRole.Translator);

        locale.updateFrom(json.toModel(locale.project));

        LOGGER.debug("Locale: {}", Json.toJson(locale));
        service.save(locale);

        return ok(Json.toJson(dto.Locale.from(locale)));
      });
    });
  }

  public Result upload(UUID localeId, String fileType) {
    return tryCatch(() -> locale(localeId, locale -> {
      injector.instanceOf(Locales.class).importLocale(locale, request());

      return ok(Json.newObject().put("status", "OK"));
    }));
  }

  public Result download(UUID localeId, String fileType) {
    return injector.instanceOf(Locales.class).download(localeId, fileType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Function<Locale, dto.Locale> dtoMapper() {
    return dto.Locale::from;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Function<UUID, Locale> getter() {
    return Locale::byId;
  }

  @Override
  protected Scope[] scopesGet() {
    return new Scope[] {Scope.ProjectRead, Scope.LocaleRead};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesCreate() {
    return new Scope[] {Scope.ProjectRead, Scope.LocaleWrite};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesDelete() {
    return new Scope[] {Scope.ProjectRead, Scope.LocaleWrite};
  }
}
