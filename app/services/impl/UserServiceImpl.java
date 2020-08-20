package services.impl;

import criterias.AccessTokenCriteria;
import criterias.LinkedAccountCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.ProjectUserCriteria;
import criterias.UserCriteria;
import io.ebean.PagedList;
import models.ActionType;
import models.Setting;
import models.User;
import models.UserStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.UserRepository;
import services.AccessTokenService;
import services.AuthProvider;
import services.CacheService;
import services.LinkedAccountService;
import services.LogEntryService;
import services.MetricService;
import services.ProjectService;
import services.ProjectUserService;
import services.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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
    return postGet(cache.getOrElseUpdate(
            User.getCacheKey(username, fetches),
            () -> userRepository.byUsername(username, fetches),
            60));
  }

  @Override
  public User saveSettings(UUID userId, Map<String, String> settings) {
    return postUpdate(userRepository.saveSettings(userId, cleanSettings(settings)));
  }

  @Override
  public User updateSettings(UUID userId, Map<String, String> settings) {
    return postUpdate(userRepository.updateSettings(userId, cleanSettings(settings)));
  }

  private Map<String, String> cleanSettings(Map<String, String> settings) {
    return settings.entrySet()
            .stream()
            .filter(setting -> Setting.of(setting.getKey()) != null)
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserStats getUserStats(UUID userId) {
    return log(
            () -> cache.getOrElseUpdate(
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
  protected User postUpdate(User t) {
    super.postUpdate(t);

    metricService.logEvent(User.class, ActionType.Update);

    // When user has been updated, the user cache needs to be invalidated
    cache.removeByPrefix("user:criteria:");

    return byId(t.id).updateFrom(t);
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
