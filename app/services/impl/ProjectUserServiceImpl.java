package services.impl;

import criterias.ProjectUserCriteria;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.ProjectUser;
import play.cache.CacheApi;
import repositories.ProjectUserRepository;
import services.LogEntryService;
import services.ProjectUserService;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class ProjectUserServiceImpl extends
    AbstractModelService<ProjectUser, Long, ProjectUserCriteria> implements ProjectUserService {

  private final ProjectUserRepository projectUserRepository;

  @Inject
  public ProjectUserServiceImpl(Validator validator, CacheApi cache,
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
}
