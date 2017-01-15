package controllers;

import java.util.UUID;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.ProjectCriteria;
import models.Project;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;
import services.ProjectService;
import services.UserService;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@With(ApiAction.class)
public class ProjectsApi extends Api<Project, dto.Project, UUID> {
  @Inject
  public ProjectsApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, ProjectService projectService) {
    super(injector, cache, auth, userService, logEntryService, projectService, Project::byId,
        dto.Project.class, dto.Project::from, Project::from, new Scope[] {Scope.ProjectRead},
        new Scope[] {Scope.ProjectWrite});
  }

  public Result find() {
    return toJsons(dtoMapper, () -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead);

      return Project.findBy(new ProjectCriteria().withMemberId(User.loggedInUserId()));
    });
  }
}
