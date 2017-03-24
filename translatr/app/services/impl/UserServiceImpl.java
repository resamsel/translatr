package services.impl;

import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;

import criterias.AccessTokenCriteria;
import criterias.LinkedAccountCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.ProjectUserCriteria;
import criterias.UserCriteria;
import models.AccessToken;
import models.ActionType;
import models.LinkedAccount;
import models.LogEntry;
import models.ProjectUser;
import models.User;
import models.UserStats;
import play.Configuration;
import play.cache.CacheApi;
import services.AccessTokenService;
import services.LinkedAccountService;
import services.LogEntryService;
import services.ProjectService;
import services.ProjectUserService;
import services.UserService;

/**
 *
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


  /**
   * @param configuration
   */
  @Inject
  public UserServiceImpl(Configuration configuration, Validator validator, CacheApi cache,
      LinkedAccountService linkedAccountService, AccessTokenService accessTokenService,
      ProjectService projectService, ProjectUserService projectUserService,
      LogEntryService logEntryService) {
    super(configuration, validator, logEntryService);
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
    return User.pagedBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User byId(UUID id, String... fetches) {
    return cache.getOrElse(User.getCacheKey(id), () -> User.byId(id), 60);
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
      if (name != null)
        user.name = name;
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
    if (authUser == null)
      return null;

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
    if (t.email != null)
      t.email = t.email.toLowerCase();
    if (t.username == null && t.email != null)
      t.username = emailToUsername(t.email);
    if (update)
      logEntryService.save(
          LogEntry.from(ActionType.Update, null, dto.User.class, toDto(byId(t.id)), toDto(t)));
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
  private String emailToUsername(String email) {
    String username = email.toLowerCase().replaceAll("@", "").replaceAll("\\.", "");

    // TODO: potentially slow, replace with better variant (get all users with username like
    // $username% and iterate
    // over them)
    String suffix = "";
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int retries = 10, i = 0;
    while (byUsername(String.format("%s%s", username, suffix)) != null && i++ < retries)
      suffix = String.valueOf(random.nextInt(1000));

    return String.format("%s%s", username, suffix);
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
        .save(LinkedAccount.findBy(new LinkedAccountCriteria().withUserId(otherUser.id)).stream()
            .map(linkedAccount -> linkedAccount.withUser(user)).collect(toList()));
    otherUser.linkedAccounts.clear();

    accessTokenService.save(AccessToken.findBy(new AccessTokenCriteria().withUserId(otherUser.id))
        .stream().map(accessToken -> accessToken.withUser(user)).collect(toList()));

    logEntryService.save(LogEntry.findBy(new LogEntryCriteria().withUserId(otherUser.id)).stream()
        .filter(logEntry -> !logEntry.contentType.equals("dto.User"))
        .map(logEntry -> logEntry.withUser(user)).collect(toList()));

    projectService
        .save(
            projectService.findBy(new ProjectCriteria().withOwnerId(otherUser.id)).getList()
                .stream()
                .map(project -> project.withOwner(user)
                    .withName(String.format("%s (%s)", project.name, user.email)))
                .collect(toList()));

    projectUserService.save(ProjectUser.pagedBy(new ProjectUserCriteria().withUserId(otherUser.id))
        .getList().stream().map(member -> member.withUser(user)).collect(toList()));


    // deactivate the merged user that got added to this one
    otherUser.active = false;
    save(Arrays.asList(new User[] {otherUser, user}));

    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User byUsername(String username) {
    return log(() -> cache.getOrElse(String.format("username:%s", username),
        () -> User.byUsername(username), 60), LOGGER, "byUsername");
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
