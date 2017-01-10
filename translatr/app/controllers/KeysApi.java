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
import criterias.KeyCriteria;
import models.Key;
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
import services.KeyService;
import services.LogEntryService;
import services.UserService;
import utils.JsonUtils;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@With(ApiAction.class)
public class KeysApi extends Api<Key, dto.Key, UUID> {
  private static final Logger LOGGER = LoggerFactory.getLogger(KeysApi.class);

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   * @param logEntryService
   * @param keyService
   */
  @Inject
  public KeysApi(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, KeyService keyService) {
    super(injector, cache, auth, userService, logEntryService, keyService);
  }

  public Result find(UUID projectId) {
    return project(projectId, project -> {
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Translator,
          ProjectRole.Developer);

      return toJsons(dto.Key::from, finder(new KeyCriteria().withProjectId(project.id), Key::findBy,
          Scope.ProjectRead, Scope.KeyRead));
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Supplier<Key> creator(Request request) {
    JsonNode json = request().body().asJson();

    return project(JsonUtils.getUuid(json, "projectId"), project -> {
      checkPermissionAll("Access token not allowed", scopesCreate());
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Developer);

      return () -> service.create(json);
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result update() {
    return loggedInUser(user -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.KeyWrite);

      dto.Key json = Json.fromJson(request().body().asJson(), dto.Key.class);
      if (json.id == null)
        throw new IllegalArgumentException("Field 'id' required");

      return key(json.id, key -> {
        checkProjectRole(key.project, user, ProjectRole.Owner, ProjectRole.Developer);

        key.updateFrom(json.toModel(key.project));

        LOGGER.debug("Locale: {}", Json.toJson(key));
        service.save(key);

        return ok(Json.toJson(dto.Key.from(key)));
      });
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Function<Key, dto.Key> dtoMapper() {
    return dto.Key::from;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Function<UUID, Key> getter() {
    return Key::byId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesGet() {
    return new Scope[] {Scope.ProjectRead, Scope.KeyRead};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesCreate() {
    return new Scope[] {Scope.ProjectRead, Scope.KeyWrite};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesDelete() {
    return new Scope[] {Scope.ProjectRead, Scope.KeyWrite};
  }
}
