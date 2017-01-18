package controllers;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

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
public class KeysApi extends Api<Key, UUID, KeyCriteria, dto.Key> {
  @Inject
  public KeysApi(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, KeyService keyService) {
    super(injector, cache, auth, userService, logEntryService, keyService, Key::byId, Key::findBy,
        dto.Key.class, dto.Key::from, Key::from, new Scope[] {Scope.ProjectRead, Scope.KeyRead},
        new Scope[] {Scope.ProjectRead, Scope.KeyWrite});
  }

  public CompletionStage<Result> find(UUID projectId) {
    return findBy(
        new KeyCriteria().withProjectId(projectId).withSearch(request().getQueryString("search")),
        criteria -> checkProjectRole(projectId, User.loggedInUser(), ProjectRole.Owner,
            ProjectRole.Translator, ProjectRole.Developer));
  }
}
