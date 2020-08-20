package services.impl;

import io.ebean.PagedList;
import criterias.ProjectUserCriteria;
import models.ActionType;
import models.ProjectUser;
import repositories.ProjectUserRepository;
import services.*;

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

  @Inject
  public ProjectUserServiceImpl(Validator validator, CacheService cache,
                                ProjectUserRepository projectUserRepository, LogEntryService logEntryService,
                                AuthProvider authProvider, MetricService metricService) {
    super(validator, cache, projectUserRepository, ProjectUser::getCacheKey, logEntryService, authProvider);

    this.projectUserRepository = projectUserRepository;
    this.metricService = metricService;
  }

  @Override
  public int countBy(ProjectUserCriteria criteria) {
    return cache.getOrElseUpdate(
            criteria.getCacheKey(),
            () -> projectUserRepository.countBy(criteria),
            60);
  }

  @Override
  protected PagedList<ProjectUser> postFind(PagedList<ProjectUser> pagedList) {
    metricService.logEvent(ProjectUser.class, ActionType.Read);

    return super.postFind(pagedList);
  }

  @Override
  protected ProjectUser postGet(ProjectUser projectUser) {
    metricService.logEvent(ProjectUser.class, ActionType.Read);

    return super.postGet(projectUser);
  }

  @Override
  protected void postCreate(ProjectUser t) {
    super.postCreate(t);

    metricService.logEvent(ProjectUser.class, ActionType.Create);

    cache.removeByPrefix("member:criteria:" + t.project.id);
  }

  @Override
  protected ProjectUser postUpdate(ProjectUser t) {
    super.postUpdate(t);

    metricService.logEvent(ProjectUser.class, ActionType.Update);

    // When locale has been updated, the locale cache needs to be invalidated
    cache.removeByPrefix("member:criteria:" + t.project.id);

    return t;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(ProjectUser t) {
    super.postDelete(t);

    metricService.logEvent(ProjectUser.class, ActionType.Delete);

    // When member has been deleted, the key cache needs to be invalidated
    cache.removeByPrefix("member:criteria:" + t.project.id);
  }
}
