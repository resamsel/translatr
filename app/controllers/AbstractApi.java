package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.PermissionException;
import models.Project;
import models.ProjectRole;
import models.User;
import play.Configuration;
import play.inject.Injector;
import services.CacheService;
import services.api.ApiService;

import java.util.UUID;

public abstract class AbstractApi<DTO extends Dto, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>, API extends ApiService<DTO, ID, CRITERIA>>
    extends AbstractBaseApi {
  protected final API api;
  protected final Configuration configuration;

  protected AbstractApi(Injector injector, CacheService cache, PlayAuthenticate auth,
      API api) {
    super(injector, cache, auth);

    this.configuration = injector.instanceOf(Configuration.class);
    this.api = api;
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
