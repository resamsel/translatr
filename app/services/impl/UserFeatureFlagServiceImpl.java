package services.impl;

import actors.ActivityActorRef;
import io.ebean.PagedList;
import criterias.UserFeatureFlagCriteria;
import models.ActionType;
import models.User;
import models.UserFeatureFlag;
import models.UserRole;
import play.mvc.Http;
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
          Validator validator,
          CacheService cache,
          AuthProvider authProvider,
          UserFeatureFlagRepository userFeatureFlagRepository,
          LogEntryService logEntryService,
          MetricService metricService,
          ActivityActorRef activityActor) {
    super(validator, cache, userFeatureFlagRepository, UserFeatureFlag::getCacheKey, logEntryService, authProvider, activityActor);

    this.metricService = metricService;
  }

  @Override
  public PagedList<UserFeatureFlag> findBy(UserFeatureFlagCriteria criteria) {
    User loggedInUser = authProvider.loggedInUser(criteria.getRequest());
    if (loggedInUser != null && loggedInUser.role != UserRole.Admin) {
      criteria.setUserId(loggedInUser.id);
    }

    return super.findBy(criteria);
  }

  @Override
  protected PagedList<UserFeatureFlag> postFind(PagedList<UserFeatureFlag> pagedList, Http.Request request) {
    metricService.logEvent(UserFeatureFlag.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  @Override
  protected UserFeatureFlag postGet(UserFeatureFlag userFeatureFlag, Http.Request request) {
    metricService.logEvent(UserFeatureFlag.class, ActionType.Read);

    return super.postGet(userFeatureFlag, request);
  }

  @Override
  protected void postCreate(UserFeatureFlag t, Http.Request request) {
    super.postCreate(t, request);

    metricService.logEvent(UserFeatureFlag.class, ActionType.Create);
  }

  @Override
  protected void preSave(UserFeatureFlag t, Http.Request request) {
    super.preSave(t, request);

    User loggedInUser = authProvider.loggedInUser(request);
    if (t.user == null || t.user.id == null
            || (loggedInUser != null && t.user.id != loggedInUser.id && loggedInUser.role != UserRole.Admin)) {
      // only allow admins to create access tokens for other users
      t.user = loggedInUser;
    }
  }

  @Override
  protected UserFeatureFlag postUpdate(UserFeatureFlag t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(UserFeatureFlag.class, ActionType.Update);

    return t;
  }

  @Override
  protected void postDelete(UserFeatureFlag t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(UserFeatureFlag.class, ActionType.Delete);
  }
}
