package services.impl;

import criterias.ProjectUserCriteria;
import models.ProjectUser;
import repositories.ProjectUserRepository;
import services.CacheService;
import services.LogEntryService;
import services.ProjectUserService;

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

  @Inject
  public ProjectUserServiceImpl(Validator validator, CacheService cache,
      ProjectUserRepository projectUserRepository, LogEntryService logEntryService) {
    super(validator, cache, projectUserRepository, ProjectUser::getCacheKey, logEntryService);

    this.projectUserRepository = projectUserRepository;
  }

  @Override
  public int countBy(ProjectUserCriteria criteria) {
    return cache.getOrElse(
        criteria.getCacheKey(),
        () -> projectUserRepository.countBy(criteria),
        60);
  }

  @Override
  protected void postCreate(ProjectUser t) {
    super.postCreate(t);

    cache.removeByPrefix("member:criteria:" + t.project.id);
  }

  @Override
  protected void preCreate(ProjectUser t) {
    super.preCreate(t);
  }

  @Override
  protected void postUpdate(ProjectUser t) {
    super.postUpdate(t);

    // When locale has been updated, the locale cache needs to be invalidated
    cache.removeByPrefix("member:criteria:" + t.project.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(ProjectUser t) {
    super.postDelete(t);

    // When member has been deleted, the key cache needs to be invalidated
    cache.removeByPrefix("member:criteria:" + t.project.id);
  }
}
