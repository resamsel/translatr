package controllers;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.ProjectCriteria;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
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
@io.swagger.annotations.Api(value = "project", produces = "application/json")
@With(ApiAction.class)
public class ProjectsApi extends Api<Project, UUID, ProjectCriteria, dto.Project> {
  @Inject
  public ProjectsApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, ProjectService projectService) {
    super(injector, cache, auth, userService, logEntryService, projectService, Project::byId,
        Project::findBy, dto.Project.class, dto.Project::from, Project::from,
        new Scope[] {Scope.ProjectRead}, new Scope[] {Scope.ProjectWrite});
  }

  @ApiOperation(value = "Find projects", authorizations = @Authorization(value = "scopes",
      scopes = @AuthorizationScope(scope = "project:read", description = "Read project")))
  @ApiResponses({
      @ApiResponse(code = 200, message = "Found projects", response = dto.Project[].class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "search", value = "Part of the name of the projects",
          dataType = "string", paramType = "query")})
  public CompletionStage<Result> find() {
    return findBy(new ProjectCriteria().withMemberId(User.loggedInUserId())
        .withSearch(request().getQueryString("search")));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Get project by ID", authorizations = @Authorization(value = "scopes",
      scopes = @AuthorizationScope(scope = "project:read", description = "Read project")))
  @ApiResponses({@ApiResponse(code = 200, message = "Found project", response = dto.Project.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class),
      @ApiResponse(code = 404, message = "Project not found", response = dto.Error.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> get(@ApiParam(value = "The project ID") UUID id) {
    return super.get(id);
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Create project", authorizations = @Authorization(value = "scopes",
      scopes = {@AuthorizationScope(scope = "project:write", description = "Write project")}))
  @ApiResponses({
      @ApiResponse(code = 200, message = "Created project", response = dto.Project.class),
      @ApiResponse(code = 400, message = "Bad request", response = dto.Error.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = "The project to create", required = true,
          dataType = "dto.Project", paramType = "body"),
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> create() {
    return super.create();
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Update project", authorizations = @Authorization(value = "scopes",
      scopes = {@AuthorizationScope(scope = "project:write", description = "Write project")}))
  @ApiResponses({
      @ApiResponse(code = 200, message = "Updated project", response = dto.Project.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = "The project to update", required = true,
          dataType = "dto.Project", paramType = "body"),
      @ApiImplicitParam(name = "access_token", value = "The access token", required = true,
          dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> update() {
    return super.update();
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = "Delete project", authorizations = @Authorization(value = "scopes",
      scopes = {@AuthorizationScope(scope = "project:write", description = "Write project")}))
  @ApiResponses({
      @ApiResponse(code = 200, message = "Deleted project", response = dto.Project.class),
      @ApiResponse(code = 403, message = "Invalid access token", response = dto.Error.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = "access_token", value = "The access token",
      required = true, dataType = "string", paramType = "query")})
  @Override
  public CompletionStage<Result> delete(@ApiParam(value = "The project ID") UUID id) {
    return super.delete(id);
  }
}
