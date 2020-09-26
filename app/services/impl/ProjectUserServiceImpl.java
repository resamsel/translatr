package services.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol;
import criterias.GetCriteria;
import criterias.ProjectUserCriteria;
import dto.PermissionException;
import io.ebean.PagedList;
import mappers.ProjectUserMapper;
import models.ActionType;
import models.ProjectUser;
import play.mvc.Http;
import repositories.ProjectUserRepository;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;
import services.MetricService;
import services.ProjectUserService;
import validators.ProjectUserModifyAllowedValidator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class ProjectUserServiceImpl extends
        AbstractModelService<ProjectUser, Long, ProjectUserCriteria> implements ProjectUserService {

  private final ProjectUserRepository projectUserRepository;
  private final MetricService metricService;
  private final ProjectUserModifyAllowedValidator projectUserModifyAllowedValidator;

  @Inject
  public ProjectUserServiceImpl(
          Validator validator,
          CacheService cache,
          ProjectUserRepository projectUserRepository,
          LogEntryService logEntryService,
          AuthProvider authProvider,
          MetricService metricService,
          ActivityActorRef activityActor,
          ProjectUserModifyAllowedValidator projectUserModifyAllowedValidator) {
    super(validator, cache, projectUserRepository, ProjectUser::getCacheKey, logEntryService, authProvider, activityActor);

    this.projectUserRepository = projectUserRepository;
    this.metricService = metricService;
    this.projectUserModifyAllowedValidator = projectUserModifyAllowedValidator;
  }

  @Override
  public int countBy(ProjectUserCriteria criteria) {
    return cache.getOrElseUpdate(
            criteria.getCacheKey(),
            () -> projectUserRepository.countBy(criteria),
            60);
  }

  @Override
  protected PagedList<ProjectUser> postFind(PagedList<ProjectUser> pagedList, Http.Request request) {
    metricService.logEvent(ProjectUser.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  @Override
  protected ProjectUser postGet(ProjectUser projectUser, Http.Request request) {
    metricService.logEvent(ProjectUser.class, ActionType.Read);

    return super.postGet(projectUser, request);
  }

  @Override
  protected void preCreate(ProjectUser t, Http.Request request) {
    super.preCreate(t, request);

    if (!projectUserModifyAllowedValidator.isValid(t, request)) {
      throw new PermissionException("member.invalid");
    }
  }

  @Override
  protected void postCreate(ProjectUser t, Http.Request request) {
    super.postCreate(t, request);

    metricService.logEvent(ProjectUser.class, ActionType.Create);

    cache.removeByPrefix("member:criteria:" + t.project.id);

    activityActor.tell(
            new ActivityProtocol.Activity<>(
                    ActionType.Create, authProvider.loggedInUser(request), t.project, dto.ProjectUser.class, null, toDto(t)
            ),
            null
    );
  }

  @Override
  protected void preUpdate(ProjectUser t, Http.Request request) {
    super.preUpdate(t, request);

    if (!projectUserModifyAllowedValidator.isValid(t, request)) {
      throw new PermissionException("member.invalid");
    }

    activityActor.tell(
            new ActivityProtocol.Activity<>(
                    ActionType.Update, authProvider.loggedInUser(request), t.project, dto.ProjectUser.class, toDto(byId(GetCriteria.from(t.id, request))), toDto(t)
            ),
            null
    );
  }

  @Override
  protected ProjectUser postUpdate(ProjectUser t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(ProjectUser.class, ActionType.Update);

    // When locale has been updated, the locale cache needs to be invalidated
    cache.removeByPrefix("member:criteria:" + t.project.id);

    return t;
  }

  @Override
  protected void preDelete(ProjectUser t, Http.Request request) {
    super.preDelete(t, request);

    activityActor.tell(
            new ActivityProtocol.Activity<>(
                    ActionType.Delete, authProvider.loggedInUser(request), t.project, dto.ProjectUser.class, toDto(t), null
            ),
            null
    );
  }

  @Override
  protected void postDelete(ProjectUser t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(ProjectUser.class, ActionType.Delete);

    // When member has been deleted, the key cache needs to be invalidated
    cache.removeByPrefix("member:criteria:" + t.project.id);
  }

  private dto.ProjectUser toDto(ProjectUser t) {
    return ProjectUserMapper.toDto(t);
  }
}
