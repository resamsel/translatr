package services.impl;

import com.avaje.ebean.PagedList;
import criterias.UserFeatureFlagCriteria;
import models.User;
import models.UserFeatureFlag;
import models.UserRole;
import repositories.UserFeatureFlagRepository;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;
import services.UserFeatureFlagService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.UUID;

@Singleton
public class UserFeatureFlagServiceImpl extends
        AbstractModelService<UserFeatureFlag, UUID, UserFeatureFlagCriteria> implements UserFeatureFlagService {

  @Inject
  public UserFeatureFlagServiceImpl(
          Validator validator, CacheService cache, AuthProvider authProvider,
          UserFeatureFlagRepository userFeatureFlagRepository, LogEntryService logEntryService) {
    super(validator, cache, userFeatureFlagRepository, UserFeatureFlag::getCacheKey, logEntryService, authProvider);
  }

  @Override
  public PagedList<UserFeatureFlag> findBy(UserFeatureFlagCriteria criteria) {
    User loggedInUser = authProvider.loggedInUser();
    if (loggedInUser != null && loggedInUser.role != UserRole.Admin) {
      criteria.setUserId(loggedInUser.id);
    }

    return super.findBy(criteria);
  }
}
