package services.impl;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.user.*;
import criterias.*;
import models.ActionType;
import models.LinkedAccount;
import models.User;
import models.UserStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.UserRepository;
import services.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

/**
 * @author resamsel
 * @version 1 Oct 2016
 */
@Singleton
public class UserServiceImpl extends AbstractModelService<User, UUID, UserCriteria>
        implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final LinkedAccountService linkedAccountService;
  private final AccessTokenService accessTokenService;
  private final ProjectService projectService;
  private final ProjectUserService projectUserService;
  private final MetricService metricService;

  @Inject
  public UserServiceImpl(Validator validator, CacheService cache, UserRepository userRepository,
                         LinkedAccountService linkedAccountService, AccessTokenService accessTokenService,
                         ProjectService projectService, ProjectUserService projectUserService,
                         LogEntryService logEntryService, AuthProvider authProvider, MetricService metricService) {
    super(validator, cache, userRepository, User::getCacheKey, logEntryService, authProvider);

    this.userRepository = userRepository;
    this.linkedAccountService = linkedAccountService;
    this.accessTokenService = accessTokenService;
    this.projectService = projectService;
    this.projectUserService = projectUserService;
    this.metricService = metricService;
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
      Optional.ofNullable(identity.getName())
              .ifPresent(name -> user.name = name);
    }

    if (authUser instanceof UserRoleIdentity) {
      final UserRoleIdentity identity = (UserRoleIdentity) authUser;
      Optional.ofNullable(identity.getUserRole())
              .ifPresent(role -> user.role = role);
    }

    if (authUser instanceof PreferredUsernameIdentity) {
      final PreferredUsernameIdentity identity = (PreferredUsernameIdentity) authUser;
      Optional.ofNullable(identity.getPreferredUsername())
              .ifPresent(preferredUsername -> user.username = userRepository.uniqueUsername(preferredUsername));
    }

    return create(user);
  }

  @Override
  public User addLinkedAccount(final AuthUserIdentity oldUser, final AuthUserIdentity newUser) {
    final User u = getLocalUser(oldUser);
    u.linkedAccounts.add(LinkedAccount.createFrom(newUser));
    return create(u);
  }

  @Override
  public User getLocalUser(final AuthUserIdentity authUser) {
    if (authUser == null) {
      return null;
    }

    return log(
            () -> cache.getOrElse(
                    String.format("%s:%s", authUser.getProvider(), authUser.getId()),
                    () -> userRepository.findByAuthUserIdentity(authUser),
                    10 * 60
            ),
            LOGGER,
            "getLocalUser"
    );
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

  @Override
  public User merge(final AuthUserIdentity oldUser, final AuthUserIdentity newUser) {
    return merge(getLocalUser(oldUser), getLocalUser(newUser));
  }

  /**
   * Do merging stuff here - like resources, etc.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public User merge(final User user, final User otherUser) {
    linkedAccountService.save(
            linkedAccountService.findBy(new LinkedAccountCriteria()
                    .withUserId(Objects.requireNonNull(otherUser, "other user").id))
                    .getList()
                    .stream()
                    .map(linkedAccount -> linkedAccount.withUser(user))
                    .collect(toList())
    );
    otherUser.linkedAccounts.clear();

    accessTokenService
            .save(accessTokenService.findBy(new AccessTokenCriteria().withUserId(otherUser.id))
                    .getList().stream().map(accessToken -> accessToken.withUser(user)).collect(toList()));

    logEntryService
            .save(logEntryService.findBy(new LogEntryCriteria().withUserId(otherUser.id)).getList()
                    .stream().filter(logEntry -> !logEntry.contentType.equals("User"))
                    .map(logEntry -> logEntry.withUser(user)).collect(toList()));

    projectService
            .save(
                    projectService.findBy(new ProjectCriteria().withOwnerId(otherUser.id)).getList()
                            .stream()
                            .map(project -> project.withOwner(user)
                                    .withName(String.format("%s (%s)", project.name, user.email)))
                            .collect(toList()));

    projectUserService
            .save(projectUserService.findBy(new ProjectUserCriteria().withUserId(otherUser.id))
                    .getList().stream().map(member -> member.withUser(user)).collect(toList()));

    // deactivate the merged user that got added to this one
    otherUser.active = false;
    save(Arrays.asList(otherUser, user));

    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User byUsername(String username, String... fetches) {
    return postGet(cache.getOrElse(
            User.getCacheKey(username, fetches),
            () -> userRepository.byUsername(username, fetches),
            60));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserStats getUserStats(UUID userId) {
    return log(
            () -> cache.getOrElse(
                    String.format("user:stats:%s", userId),
                    () -> UserStats.create(
                            projectUserService.countBy(new ProjectUserCriteria().withUserId(userId)),
                            logEntryService.countBy(new LogEntryCriteria().withUserId(userId))
                    ),
                    60
            ),
            LOGGER,
            "getUserStats");
  }

  @Override
  protected PagedList<User> postFind(PagedList<User> pagedList) {
    metricService.logEvent(User.class, ActionType.Read);

    return super.postFind(pagedList);
  }

  @Override
  protected User postGet(User user) {
    metricService.logEvent(User.class, ActionType.Read);

    return super.postGet(user);
  }

  @Override
  protected void preCreate(User t) {
    User cached = cache.get(User.getCacheKey(t.getId()));
    if (cached != null) {
      cache.removeByPrefix(User.getCacheKey(cached.username));
    }
  }

  @Override
  protected void postCreate(User t) {
    super.postCreate(t);

    metricService.logEvent(User.class, ActionType.Create);

    // When user has been created
    cache.removeByPrefix("user:criteria:");
  }

  @Override
  protected void postUpdate(User t) {
    super.postUpdate(t);

    metricService.logEvent(User.class, ActionType.Update);

    // When user has been updated, the user cache needs to be invalidated
    cache.removeByPrefix("user:criteria:");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(User t) {
    super.postDelete(t);

    metricService.logEvent(User.class, ActionType.Delete);

    // When locale has been deleted, the locale cache needs to be invalidated
    cache.removeByPrefix("user:criteria:");
  }
}
