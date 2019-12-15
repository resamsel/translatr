package repositories.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol.Activity;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import criterias.AccessTokenCriteria;
import criterias.PagedListFactory;
import mappers.AccessTokenMapper;
import models.AccessToken;
import models.ActionType;
import models.User;
import models.UserRole;
import org.apache.commons.lang3.StringUtils;
import repositories.AccessTokenRepository;
import repositories.Persistence;
import services.AuthProvider;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Singleton
public class AccessTokenRepositoryImpl extends
    AbstractModelRepository<AccessToken, Long, AccessTokenCriteria> implements
    AccessTokenRepository {

  public final Find<Long, AccessToken> find = new Find<Long, AccessToken>() {
  };

  @Override
  public PagedList<AccessToken> findBy(AccessTokenCriteria criteria) {
    ExpressionList<AccessToken> query = fetch().where();

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.ilike("name", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return PagedListFactory.create(query);
  }

  @Inject
  public AccessTokenRepositoryImpl(Persistence persistence,
                                   Validator validator,
                                   AuthProvider authProvider,
                                   ActivityActorRef activityActor) {
    super(persistence, validator, authProvider, activityActor);
  }

  @Override
  public AccessToken byId(Long id, String... fetches) {
    return fetch().setId(id).findUnique();
  }

  @Override
  public AccessToken byKey(String key) {
    return fetch().where().eq("key", key).findUnique();
  }

  @Override
  public AccessToken byUserAndName(UUID userId, String name) {
    return find.where().eq("user.id", userId).eq("name", name).findUnique();
  }

  private Query<AccessToken> fetch() {
    return QueryUtils.fetch(find.query().setDisableLazyLoading(true), PROPERTIES_TO_FETCH);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(AccessToken t, boolean update) {
    User loggedInUser = authProvider.loggedInUser();
    if (t.user == null || t.user.id == null
        || (loggedInUser != null && t.user.id != loggedInUser.id && loggedInUser.role != UserRole.Admin)) {
      // only allow admins to create access tokens for other users
      t.user = loggedInUser;
    }
    if (StringUtils.isBlank(t.key)) {
      t.key = generateKey(AccessToken.KEY_LENGTH);
    }
  }

  @Override
  protected void prePersist(AccessToken t, boolean update) {
    if (update) {
      activityActor.tell(
          new Activity<>(
              ActionType.Update,
              authProvider.loggedInUser(),
              null,
              dto.AccessToken.class,
              AccessTokenMapper.toDto(byId(t.id)),
              AccessTokenMapper.toDto(t)
          ),
          null
      );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(AccessToken t, boolean update) {
    if (!update) {
      activityActor.tell(
          new Activity<>(
              ActionType.Create,
              authProvider.loggedInUser(),
              null,
              dto.AccessToken.class,
              null,
              AccessTokenMapper.toDto(t)
          ),
          null
      );
    }
  }

  public static String generateKey(int length) {
    String raw = Base64.getEncoder().encodeToString(String
        .format("%s%s", UUID.randomUUID(), UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));

    if (raw.length() > length) {
      raw = raw.substring(0, length);
    }

    return raw.replace("+", "/");
  }
}
