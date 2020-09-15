package controllers;

import com.typesafe.config.Config;
import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.PermissionException;
import models.Project;
import models.ProjectRole;
import models.User;
import play.inject.Injector;
import services.AuthProvider;
import services.PermissionService;
import services.api.ApiService;

import java.util.UUID;

public abstract class AbstractApi<DTO extends Dto, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>, API extends ApiService<DTO, ID, CRITERIA>>
        extends AbstractBaseApi {
  protected final AuthProvider authProvider;
  protected final API api;
  protected final Config configuration;
  private final PermissionService permissionService;

  protected AbstractApi(Injector injector, AuthProvider authProvider, API api) {
    super(injector);

    this.configuration = injector.instanceOf(Config.class);
    this.authProvider = authProvider;
    this.api = api;
    this.permissionService = injector.instanceOf(PermissionService.class);
  }

  protected void checkProjectRole(UUID projectId, User user, ProjectRole... roles) {
    if (!permissionService.hasPermissionAny(projectId, user, roles))
      throw new PermissionException("User not allowed in project");
  }

  protected void checkProjectRole(Project project, User user, ProjectRole... roles) {
    if (!permissionService.hasPermissionAny(project, user, roles))
      throw new PermissionException("User not allowed in project");
  }
}
