package repositories.impl;

import actors.ActivityActorRef;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import criterias.PagedListFactory;
import criterias.UserFeatureFlagCriteria;
import models.User;
import models.UserFeatureFlag;
import models.UserRole;
import org.apache.commons.lang3.StringUtils;
import repositories.Persistence;
import repositories.UserFeatureFlagRepository;
import services.AuthProvider;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.UUID;

@Singleton
public class UserFeatureFlagRepositoryImpl extends
        AbstractModelRepository<UserFeatureFlag, UUID, UserFeatureFlagCriteria> implements
        UserFeatureFlagRepository {

  public final Find<UUID, UserFeatureFlag> find = new Find<UUID, UserFeatureFlag>() {
  };

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

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.ilike("name", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return PagedListFactory.create(query);
  }

  @Override
  public UserFeatureFlag byId(UUID id, String... fetches) {
    return fetch().setId(id).findUnique();
  }

  private Query<UserFeatureFlag> fetch() {
    return QueryUtils.fetch(find.query().setDisableLazyLoading(true), PROPERTIES_TO_FETCH);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(UserFeatureFlag t, boolean update) {
    User loggedInUser = authProvider.loggedInUser();
    if (t.user == null || t.user.id == null
            || (loggedInUser != null && t.user.id != loggedInUser.id && loggedInUser.role != UserRole.Admin)) {
      // only allow admins to create access tokens for other users
      t.user = loggedInUser;
    }
  }
}
