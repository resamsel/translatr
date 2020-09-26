package repositories.impl;

import actors.ActivityActorRef;
import criterias.ContextCriteria;
import criterias.PagedListFactory;
import criterias.UserFeatureFlagCriteria;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import models.Feature;
import models.User;
import models.UserFeatureFlag;
import models.UserRole;
import org.apache.commons.lang3.StringUtils;
import repositories.Persistence;
import repositories.UserFeatureFlagRepository;
import services.AuthProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Singleton
public class UserFeatureFlagRepositoryImpl extends
        AbstractModelRepository<UserFeatureFlag, UUID, UserFeatureFlagCriteria> implements
        UserFeatureFlagRepository {

  @Inject
  public UserFeatureFlagRepositoryImpl(Persistence persistence,
                                       Validator validator,
                                       AuthProvider authProvider,
                                       ActivityActorRef activityActor) {
    super(persistence, validator, authProvider, activityActor);
  }

  @Override
  public PagedList<UserFeatureFlag> findBy(UserFeatureFlagCriteria criteria) {
    ExpressionList<UserFeatureFlag> query = fetch().where();

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (criteria.getFeature() != null) {
      query.eq("feature", criteria.getFeature());
    }

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.ilike("feature", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return PagedListFactory.create(query);
  }

  @Override
  public UserFeatureFlag byId(UUID id, String... fetches) {
    return fetch().setId(id).findOne();
  }

  @Override
  public UserFeatureFlag byUserIdAndFeature(UUID userId, Feature feature) {
    return fetch()
        .where()
        .eq("user.id", userId)
        .eq("feature", feature.getName())
        .findOne();
  }

  @Override
  protected Query<UserFeatureFlag> createQuery(ContextCriteria criteria) {
    return fetch(criteria.getFetches());
  }

  private Query<UserFeatureFlag> fetch(String... fetches) {
    return fetch(fetches != null ? Arrays.asList(fetches) : Collections.emptyList());
  }

  private Query<UserFeatureFlag> fetch(List<String> fetches) {
    return createQuery(UserFeatureFlag.class, PROPERTIES_TO_FETCH, fetches);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(UserFeatureFlag t, boolean update) {
    User loggedInUser = authProvider.loggedInUser(null) /* FIXME: will fail! */;
    if (t.user == null || t.user.id == null
            || (loggedInUser != null && t.user.id != loggedInUser.id && loggedInUser.role != UserRole.Admin)) {
      // only allow admins to create access tokens for other users
      t.user = loggedInUser;
    }
  }
}
