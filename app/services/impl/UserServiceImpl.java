package services.impl;

import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.google.common.collect.ImmutableSet;
import criterias.AccessTokenCriteria;
import criterias.LinkedAccountCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.ProjectUserCriteria;
import criterias.UserCriteria;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import models.AccessToken;
import models.ActionType;
import models.LinkedAccount;
import models.LogEntry;
import models.ProjectUser;
import models.User;
import models.UserStats;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import services.AccessTokenService;
import services.LinkedAccountService;
import services.LogEntryService;
import services.ProjectService;
import services.ProjectUserService;
import services.UserService;

/**
 * @author resamsel
 * @version 1 Oct 2016
 */
@Singleton
public class UserServiceImpl extends AbstractModelService<User, UUID, UserCriteria>
    implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  private final CacheApi cache;

  private final LinkedAccountService linkedAccountService;

  private final AccessTokenService accessTokenService;

  private final ProjectService projectService;

  private final ProjectUserService projectUserService;

  @Inject
  public UserServiceImpl(Validator validator, CacheApi cache,
      LinkedAccountService linkedAccountService, AccessTokenService accessTokenService,
      ProjectService projectService, ProjectUserService projectUserService,
      LogEntryService logEntryService) {
    super(validator, logEntryService);
    this.cache = cache;
    this.linkedAccountService = linkedAccountService;
    this.accessTokenService = accessTokenService;
    this.projectService = projectService;
    this.projectUserService = projectUserService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<User> findBy(UserCriteria criteria) {
    return User.findBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User byId(UUID id, String... fetches) {
    return cache.getOrElse(User.getCacheKey(id, fetches), () -> User.byId(id, fetches), 60);
  }

  @Override
  public User create(final AuthUserIdentity authUser) {
    final User user = new User();
    user.active = true;
    user.linkedAccounts = Collections.singletonList(LinkedAccount.createFrom(authUser));

    if (authUser instanceof EmailIdentity) {
      final EmailIdentity identity = (EmailIdentity) authUser;
      // Remember, even when getting them from FB & Co., emails should be
      // verified within the application as a security breach there might
      // break your security as well!
      if (!"null".equals(identity.getEmail())) {
        user.email = identity.getEmail();
        user.emailValidated = false;
      }
    }

    if (authUser instanceof NameIdentity) {
      final NameIdentity identity = (NameIdentity) authUser;
      final String name = identity.getName();
      if (name != null) {
        user.name = name;
      }
    }

    return save(user);
  }

  @Override
  public User addLinkedAccount(final AuthUserIdentity oldUser, final AuthUserIdentity newUser) {
    final User u = getLocalUser(oldUser);
    u.linkedAccounts.add(LinkedAccount.createFrom(newUser));
    return save(u);
  }

  @Override
  public User getLocalUser(final AuthUserIdentity authUser) {
    if (authUser == null) {
      return null;
    }

    return log(
        () -> cache.getOrElse(String.format("%s:%s", authUser.getProvider(), authUser.getId()),
            () -> User.findByAuthUserIdentity(authUser), 10 * 60),
        LOGGER, "getLocalUser");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isLocalUser(AuthUserIdentity authUser) {
    return getLocalUser(authUser) != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void logout(AuthUserIdentity authUser) {
    cache.remove(String.format("%s:%s", authUser.getProvider(), authUser.getId()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected User validate(User model) {
    // Disabling for the moment - merging users is not possible using this method...
    // if (model.id != null && !model.id.equals(User.loggedInUserId()))
    // throw new ValidationException("User is not allowed to modify another user");

    return super.validate(model);
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
      logEntryService.save(
          LogEntry.from(ActionType.Update, null, dto.User.class, toDto(byId(t.id)), toDto(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(User t, boolean update) {
    if (update) {
      // When user has been updated, the user cache needs to be invalidated
      cache.remove(User.getCacheKey(t.id));
    }
  }

  /**
   * @param email
   * @return
   */
  @Override
  public String emailToUsername(String email) {
    if (StringUtils.isEmpty(email)) {
      return null;
    }

    return uniqueUsername(email.toLowerCase().replaceAll("[@\\.-]", ""));
  }

  @Override
  public String nameToUsername(String name) {
    if (StringUtils.isEmpty(name)) {
      return null;
    }

    return uniqueUsername(name.replaceAll("[^A-Za-z0-9_-]", "").toLowerCase());
  }

  /**
   * Generate a unique username from the given proposal.
   */
  private String uniqueUsername(String username) {
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
  public User merge(final AuthUserIdentity oldUser, final AuthUserIdentity newUser) {
    return merge(getLocalUser(oldUser), getLocalUser(newUser));
  }

  /**
   * Do merging stuff here - like resources, etc.
   *
   * {@inheritDoc}
   */
  @Override
  public User merge(final User user, final User otherUser) {
    linkedAccountService
        .save(LinkedAccount.findBy(new LinkedAccountCriteria().withUserId(otherUser.id)).getList()
            .stream().map(linkedAccount -> linkedAccount.withUser(user)).collect(toList()));
    otherUser.linkedAccounts.clear();

    accessTokenService.save(AccessToken.findBy(new AccessTokenCriteria().withUserId(otherUser.id))
        .getList().stream().map(accessToken -> accessToken.withUser(user)).collect(toList()));

    logEntryService.save(LogEntry.findBy(new LogEntryCriteria().withUserId(otherUser.id)).getList()
        .stream().filter(logEntry -> !logEntry.contentType.equals("dto.User"))
        .map(logEntry -> logEntry.withUser(user)).collect(toList()));

    projectService
        .save(
            projectService.findBy(new ProjectCriteria().withOwnerId(otherUser.id)).getList()
                .stream()
                .map(project -> project.withOwner(user)
                    .withName(String.format("%s (%s)", project.name, user.email)))
                .collect(toList()));

    projectUserService.save(ProjectUser.findBy(new ProjectUserCriteria().withUserId(otherUser.id))
        .getList().stream().map(member -> member.withUser(user)).collect(toList()));

    // deactivate the merged user that got added to this one
    otherUser.active = false;
    save(Arrays.asList(new User[]{otherUser, user}));

    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User byUsername(String username, String... fetches) {
    return log(() -> cache.getOrElse(User.getCacheKey(username, fetches),
        () -> User.byUsername(username, fetches), 60), LOGGER, "byUsername");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserStats getUserStats(UUID userId) {
    return log(() -> cache.getOrElse(String.format("user:stats:%s", userId),
        () -> User.userStats(userId), 60), LOGGER, "getUserStats");
  }

  protected dto.User toDto(User t) {
    dto.User out = dto.User.from(t);

    return out;
  }
}
