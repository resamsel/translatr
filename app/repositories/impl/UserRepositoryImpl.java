package repositories.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol.Activity;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.feth.play.module.pa.user.AuthUserIdentity;
import criterias.PagedListFactory;
import criterias.UserCriteria;
import mappers.UserMapper;
import models.ActionType;
import models.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.Persistence;
import repositories.UserRepository;
import services.AuthProvider;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static utils.Stopwatch.log;

@Singleton
public class UserRepositoryImpl extends AbstractModelRepository<User, UUID, UserCriteria> implements
    UserRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImpl.class);

  private final Find<UUID, User> find = new Find<UUID, User>() {
  };
  private final AuthProvider authProvider;

  @Inject
  public UserRepositoryImpl(Persistence persistence,
                            Validator validator,
                            AuthProvider authProvider,
                            ActivityActorRef activityActor) {
    super(persistence, validator, authProvider, activityActor);

    this.authProvider = authProvider;
  }

  @Override
  public PagedList<User> findBy(UserCriteria criteria) {
    Query<User> q = find.query().setDisableLazyLoading(true);

    if (!criteria.getFetches().isEmpty()) {
      QueryUtils.fetch(q, QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, criteria.getFetches()),
          FETCH_MAP);
    }

    ExpressionList<User> query = q.where();

    query.eq("active", true);

    if (criteria.getSearch() != null) {
      query.disjunction().ilike("name", "%" + criteria.getSearch() + "%")
          .ilike("username", "%" + criteria.getSearch() + "%").endJunction();
    }

    criteria.paged(query);

    return log(() -> PagedListFactory.create(query, criteria.hasFetch(FETCH_COUNT)), LOGGER, "findBy");
  }

  @Override
  public User byId(UUID id, String... fetches) {
    return QueryUtils.fetch(find.setId(id), fetches, FETCH_MAP).findUnique();
  }

  @Override
  public User byUsername(String username, String... fetches) {
    return QueryUtils.fetch(find.query(), fetches, FETCH_MAP).where().eq("username", username)
        .findUnique();
  }

  private ExpressionList<User> getAuthUserFind(final AuthUserIdentity identity) {
    return find.where()
        .eq("active", true)
        .eq("linkedAccounts.providerUserId", identity.getId())
        .eq("linkedAccounts.providerKey", identity.getProvider());
  }

  @Override
  public User findByAuthUserIdentity(final AuthUserIdentity identity) {
    LOGGER.debug("findByAuthUserIdentity({})", identity);

    if (identity == null) {
      return null;
    }

    LOGGER.debug("Cache miss for: {}:{}", identity.getProvider(), identity.getId());

    return log(() -> getAuthUserFind(identity).findUnique(), LOGGER, "findByAuthUserIdentity");
  }

  @Override
  public String nameToUsername(String name) {
    if (StringUtils.isEmpty(name)) {
      return null;
    }

    return uniqueUsername(name.replaceAll("[^A-Za-z0-9_-]", "").toLowerCase());
  }

  @Override
  public String emailToUsername(String email) {
    if (StringUtils.isEmpty(email)) {
      return null;
    }

    return uniqueUsername(email.toLowerCase().replaceAll("[@.-]", ""));
  }

  /**
   * Generate a unique username from the given proposal.
   */
  @Override
  public String uniqueUsername(String username) {
    if (StringUtils.isEmpty(username)) {
      return null;
    }

    // TODO: potentially slow, replace with better variant (get all users with username like
    // $username% and iterate over them)
    String prefix = StringUtils.left(username, User.USERNAME_LENGTH);
    String suffix = "";
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int retries = 10, i = 0;
    while (byUsername(String.format("%s%s", prefix, suffix)) != null && i++ < retries) {
      suffix = String.valueOf(random.nextInt(1000));
      prefix = StringUtils.left(username, User.USERNAME_LENGTH - suffix.length());
    }

    return String.format("%s%s", prefix, suffix);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(User t, boolean update) {
    if (t.email != null) {
      t.email = t.email.toLowerCase();
    }
    if (t.username == null && t.email != null) {
      t.username = emailToUsername(t.email);
    }
    if (t.username == null && t.name != null) {
      t.username = nameToUsername(t.name);
    }
    if (t.username == null) {
      t.username = String.valueOf(ThreadLocalRandom.current().nextLong());
    }
  }

  @Override
  protected void prePersist(User t, boolean update) {
    if (update) {
      activityActor.tell(
          new Activity<>(ActionType.Update, authProvider.loggedInUser(), null, dto.User.class, toDto(byId(t.id)), toDto(t)),
          null
      );
    }
  }

  private dto.User toDto(User t) {
    return UserMapper.toDto(t);
  }
}
