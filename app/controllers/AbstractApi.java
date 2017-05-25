package controllers;

import java.util.UUID;
import java.util.function.Supplier;

import com.feth.play.module.pa.PlayAuthenticate;

import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.PermissionException;
import models.Project;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import services.api.ApiService;
import utils.PermissionUtils;

public abstract class AbstractApi<DTO extends Dto, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>>
    extends AbstractBaseApi {
  protected final ApiService<DTO, ID, CRITERIA> api;
  protected final Configuration configuration;

  protected AbstractApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      ApiService<DTO, ID, CRITERIA> api) {
    super(injector, cache, auth);

    this.configuration = injector.instanceOf(Configuration.class);
    this.api = api;
  }

  /**
   * @param errorMessage
   * @param scopes
   */
  protected void checkPermissionAll(String errorMessage, Scope... scopes) {
    if (!PermissionUtils.hasPermissionAll(scopes))
      throw new PermissionException(errorMessage, scopes);
  }

  protected void checkProjectRole(Project project, User user, ProjectRole... roles) {
    checkProjectRole(project.id, user, roles);
  }

  protected void checkProjectRole(UUID projectId, User user, ProjectRole... roles) {
    if (!PermissionUtils.hasPermissionAny(projectId, user, roles))
      throw new PermissionException("User not allowed in project");
  }

  @Override
  protected Result tryCatch(Supplier<Result> supplier) {
    try {
      return supplier.get();
    } catch (Throwable t) {
      return handleException(t);
    }
  }
}
