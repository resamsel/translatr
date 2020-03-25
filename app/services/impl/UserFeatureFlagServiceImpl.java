package services.impl;

import com.avaje.ebean.PagedList;
import criterias.UserFeatureFlagCriteria;
import models.ActionType;
import models.User;
import models.UserFeatureFlag;
import models.UserRole;
import repositories.UserFeatureFlagRepository;
import services.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.UUID;

@Singleton
public class UserFeatureFlagServiceImpl extends
        AbstractModelService<UserFeatureFlag, UUID, UserFeatureFlagCriteria> implements UserFeatureFlagService {

  private final MetricService metricService;

  @Inject
  public UserFeatureFlagServiceImpl(
          Validator validator, CacheService cache, AuthProvider authProvider,
          UserFeatureFlagRepository userFeatureFlagRepository, LogEntryService logEntryService,
          MetricService metricService) {
    super(validator, cache, userFeatureFlagRepository, UserFeatureFlag::getCacheKey, logEntryService, authProvider);
    this.metricService = metricService;
  }

  @Override
  public PagedList<UserFeatureFlag> findBy(UserFeatureFlagCriteria criteria) {
    User loggedInUser = authProvider.loggedInUser();
    if (loggedInUser != null && loggedInUser.role != UserRole.Admin) {
      criteria.setUserId(loggedInUser.id);
    }

    return super.findBy(criteria);
  }

  @Override
  protected PagedList<UserFeatureFlag> postFind(PagedList<UserFeatureFlag> pagedList) {
    metricService.logEvent(UserFeatureFlag.class, ActionType.Read);

    return super.postFind(pagedList);
  }

  @Override
  protected UserFeatureFlag postGet(UserFeatureFlag userFeatureFlag) {
    metricService.logEvent(UserFeatureFlag.class, ActionType.Read);

    return super.postGet(userFeatureFlag);
  }

  @Override
  protected void postCreate(UserFeatureFlag t) {
    super.postCreate(t);

    metricService.logEvent(UserFeatureFlag.class, ActionType.Create);
  }

  @Override
  protected void postUpdate(UserFeatureFlag t) {
    super.postUpdate(t);

    metricService.logEvent(UserFeatureFlag.class, ActionType.Update);
  }

  @Override
  protected void postDelete(UserFeatureFlag t) {
    super.postDelete(t);

    metricService.logEvent(UserFeatureFlag.class, ActionType.Delete);
  }
}
