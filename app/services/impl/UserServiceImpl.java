package services.impl;

import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import criterias.AccessTokenCriteria;
import criterias.LinkedAccountCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.ProjectUserCriteria;
import criterias.UserCriteria;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.LinkedAccount;
import models.User;
import models.UserStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import repositories.UserRepository;
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

  private final UserRepository userRepository;
  private final LinkedAccountService linkedAccountService;

  private final AccessTokenService accessTokenService;

  private final ProjectService projectService;

  private final ProjectUserService projectUserService;

  @Inject
  public UserServiceImpl(Validator validator, CacheApi cache, UserRepository userRepository,
      LinkedAccountService linkedAccountService, AccessTokenService accessTokenService,
      ProjectService projectService, ProjectUserService projectUserService,
      LogEntryService logEntryService) {
    super(validator, cache, userRepository, User::getCacheKey, logEntryService);

    this.cache = cache;
    this.userRepository = userRepository;
    this.linkedAccountService = linkedAccountService;
    this.accessTokenService = accessTokenService;
    this.projectService = projectService;
    this.projectUserService = projectUserService;
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
      LOGGER.debug("Auth user is null");
      return null;
    }

    return log(
        () -> cache.getOrElse(String.format("%s:%s", authUser.getProvider(), authUser.getId()),
            () -> userRepository.findByAuthUserIdentity(authUser), 10 * 60),
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

    accessTokenService
        .save(accessTokenService.findBy(new AccessTokenCriteria().withUserId(otherUser.id))
            .getList().stream().map(accessToken -> accessToken.withUser(user)).collect(toList()));

    logEntryService
        .save(logEntryService.findBy(new LogEntryCriteria().withUserId(otherUser.id)).getList()
            .stream().filter(logEntry -> !logEntry.contentType.equals("dto.User"))
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
    return log(() -> cache.getOrElse(User.getCacheKey(username, fetches),
        () -> userRepository.byUsername(username, fetches), 60), LOGGER, "byUsername");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserStats getUserStats(UUID userId) {
    return log(() -> cache.getOrElse(String.format("user:stats:%s", userId),
        () -> User.userStats(userId), 60), LOGGER, "getUserStats");
  }
}
