package controllers;

import java.util.UUID;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.KeyCriteria;
import models.Key;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.KeyService;
import services.LogEntryService;
import services.UserService;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@With(ApiAction.class)
public class KeysApi extends Api<Key, dto.Key, UUID> {
  @Inject
  public KeysApi(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, KeyService keyService) {
    super(injector, cache, auth, userService, logEntryService, keyService, Key::byId, dto.Key.class,
        dto.Key::from, Key::from, new Scope[] {Scope.ProjectRead, Scope.KeyRead},
        new Scope[] {Scope.ProjectRead, Scope.KeyWrite});
  }

  public Result find(UUID projectId) {
    return project(projectId, project -> {
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Translator,
          ProjectRole.Developer);

      return toJsons(dto.Key::from, finder(Key::findBy, new KeyCriteria().withProjectId(project.id),
          Scope.ProjectRead, Scope.KeyRead));
    });
  }
}
