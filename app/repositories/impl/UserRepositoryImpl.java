package repositories.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol.Activity;
import criterias.PagedListFactory;
import criterias.UserCriteria;
import dto.NotFoundException;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static utils.Stopwatch.log;

@Singleton
public class UserRepositoryImpl extends AbstractModelRepository<User, UUID, UserCriteria> implements
        UserRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImpl.class);

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
    Query<User> q = persistence.find(User.class).setDisableLazyLoading(true);

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
    return QueryUtils.fetch(persistence.find(User.class).setId(id), fetches, FETCH_MAP)
            .findOneOrEmpty()
            .orElse(null);
  }

  @Override
  public User byUsername(String username, String... fetches) {
    return QueryUtils.fetch(persistence.find(User.class), fetches, FETCH_MAP)
            .where()
            .eq("username", username)
            .findOneOrEmpty()
            .orElse(null);
  }

  @Override
  public User byLinkedAccount(String providerKey, String providerUserId) {
    return persistence.find(User.class)
            .where()
            .eq("linkedAccounts.providerKey", providerKey)
            .eq("linkedAccounts.providerUserId", providerUserId)
            .findOneOrEmpty()
            .orElse(null);
  }

  @Override
  public User byAccessToken(String accessTokenKey) {
    return persistence.find(User.class)
            .where()
            .eq("accessTokens.key", accessTokenKey)
            .findOneOrEmpty()
            .orElse(null);
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

  @Override
  public User saveSettings(UUID userId, Map<String, String> settings) {
    return persistSettings(userId, settings, true);
  }

  @Override
  public User updateSettings(UUID userId, Map<String, String> settings) {
    return persistSettings(userId, settings, false);
  }

  /**
   * Replaces or updates existing settings based on the given replace flag.
   */
  private User persistSettings(UUID userId, Map<String, String> settings, boolean replace) {
    User user = byId(userId);

    if (user == null) {
      throw new NotFoundException(User.class.getSimpleName(), userId);
    }

    if (settings.isEmpty()) {
      // No update necessary
      return user;
    }

    if (replace) {
      user.settings.clear();
    }
    user.settings.putAll(settings);

    return persistence.update(user);
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
