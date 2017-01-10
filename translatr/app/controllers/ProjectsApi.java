package controllers;

import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.ProjectCriteria;
import models.Project;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.BodyParser;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsApi.class);

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   * @param logEntryService
   * @param projectService
   */
  @Inject
  public ProjectsApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, ProjectService projectService) {
    super(injector, cache, auth, userService, logEntryService, projectService);
  }

  public Result find() {
    return toJsons(dtoMapper(), () -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead);

      return Project.findBy(new ProjectCriteria().withMemberId(User.loggedInUserId()));
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result update() {
    return loggedInUser(user -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectWrite);

      dto.Project json = Json.fromJson(request().body().asJson(), dto.Project.class);
      if (json.id == null)
        throw new ValidationException("Field 'id' required");

      return project(json.id, project -> {
        checkProjectRole(project, user, ProjectRole.Owner);

        project.updateFrom(json.toModel());
        project.owner = user;

        LOGGER.debug("Project: {}", Json.toJson(project));
        service.save(project);

        return ok(Json.toJson(dto.Project.from(project)));
      });
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Function<Project, dto.Project> dtoMapper() {
    return dto.Project::from;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Function<UUID, Project> getter() {
    return Project::byId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesGet() {
    return new Scope[] {Scope.ProjectRead};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesCreate() {
    return new Scope[] {Scope.ProjectWrite};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesDelete() {
    return new Scope[] {Scope.ProjectWrite};
  }
}
