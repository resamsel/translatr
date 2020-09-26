package services.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol;
import criterias.AccessTokenCriteria;
import criterias.GetCriteria;
import criterias.LinkedAccountCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.ProjectUserCriteria;
import criterias.UserCriteria;
import io.ebean.PagedList;
import mappers.UserMapper;
import models.ActionType;
import models.Setting;
import models.User;
import models.UserStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
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
import java.util.Optional;
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
  public UserServiceImpl(
          Validator validator,
          CacheService cache,
          UserRepository userRepository,
          LinkedAccountService linkedAccountService,
          AccessTokenService accessTokenService,
          ProjectService projectService,
          ProjectUserService projectUserService,
          LogEntryService logEntryService,
          AuthProvider authProvider,
          MetricService metricService,
          ActivityActorRef activityActor) {
    super(validator, cache, userRepository, User::getCacheKey, logEntryService, authProvider, activityActor);

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
  public User merge(final User user, final User otherUser, Http.Request request) {
    linkedAccountService.save(
            linkedAccountService.findBy(new LinkedAccountCriteria()
                    .withUserId(Objects.requireNonNull(otherUser, "other user").id))
                    .getList()
                    .stream()
                    .map(linkedAccount -> linkedAccount.withUser(user))
                    .collect(toList()), request
    );
    otherUser.linkedAccounts.clear();

    accessTokenService
            .save(accessTokenService.findBy(new AccessTokenCriteria().withUserId(otherUser.id))
                    .getList().stream().map(accessToken -> accessToken.withUser(user)).collect(toList()), request);

    logEntryService
            .save(logEntryService.findBy(new LogEntryCriteria().withUserId(otherUser.id)).getList()
                    .stream().filter(logEntry -> !logEntry.contentType.equals("User"))
                    .map(logEntry -> logEntry.withUser(user)).collect(toList()), request);

    projectService
            .save(
                    projectService.findBy(new ProjectCriteria().withOwnerId(otherUser.id)).getList()
                            .stream()
                            .map(project -> project.withOwner(user)
                                    .withName(String.format("%s (%s)", project.name, user.email)))
                            .collect(toList()), request);

    projectUserService
            .save(projectUserService.findBy(new ProjectUserCriteria().withUserId(otherUser.id))
                    .getList().stream().map(member -> member.withUser(user)).collect(toList()), request);

    // deactivate the merged user that got added to this one
    otherUser.active = false;
    save(Arrays.asList(otherUser, user), request);

    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User byUsername(String username, Http.Request request, String... fetches) {
    return postGet(cache.getOrElseUpdate(
            User.getCacheKey(username, fetches),
            () -> userRepository.byUsername(username, fetches),
            60), request);
  }

  @Override
  public User byLinkedAccount(String providerKey, String providerUserId) {
    return userRepository.byLinkedAccount(providerKey, providerUserId);
  }

  @Override
  public User byAccessToken(String accessTokenKey) {
    return userRepository.byAccessToken(accessTokenKey);
  }

  @Override
  public User saveSettings(UUID userId, Map<String, String> settings, Http.Request request) {
    return postUpdate(userRepository.saveSettings(userId, cleanSettings(settings)), request);
  }

  @Override
  public User updateSettings(UUID userId, Map<String, String> settings, Http.Request request) {
    return postUpdate(userRepository.updateSettings(userId, cleanSettings(settings)), request);
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
  protected PagedList<User> postFind(PagedList<User> pagedList, Http.Request request) {
    metricService.logEvent(User.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  @Override
  protected User postGet(User user, Http.Request request) {
    metricService.logEvent(User.class, ActionType.Read);

    return super.postGet(user, request);
  }

  @Override
  protected void preCreate(User t, Http.Request request) {
    super.preCreate(t, request);

    Optional<User> cached = cache.get(User.getCacheKey(t.getId()));
    cached.ifPresent(user -> cache.removeByPrefix(User.getCacheKey(user.username)));
  }

  @Override
  protected void postCreate(User t, Http.Request request) {
    super.postCreate(t, request);

    metricService.logEvent(User.class, ActionType.Create);

    // When user has been created
    cache.removeByPrefix("user:criteria:");
  }

  @Override
  protected void preUpdate(User t, Http.Request request) {
    super.preUpdate(t, request);

    activityActor.tell(
            new ActivityProtocol.Activity<>(ActionType.Update, authProvider.loggedInUser(request), null, dto.User.class, toDto(byId(GetCriteria.from(t.id, request))), toDto(t)),
            null
    );
  }

  @Override
  protected User postUpdate(User t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(User.class, ActionType.Update);

    // When user has been updated, the user cache needs to be invalidated
    cache.removeByPrefix("user:criteria:");

    return byId(GetCriteria.from(t.id, request)).updateFrom(t);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(User t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(User.class, ActionType.Delete);

    // When locale has been deleted, the locale cache needs to be invalidated
    cache.removeByPrefix("user:criteria:");
  }

  private dto.User toDto(User t) {
    return UserMapper.toDto(t);
  }
}
