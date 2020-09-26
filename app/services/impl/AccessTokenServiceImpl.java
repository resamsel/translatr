package services.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol;
import criterias.AccessTokenCriteria;
import criterias.GetCriteria;
import io.ebean.PagedList;
import mappers.AccessTokenMapper;
import models.AccessToken;
import models.ActionType;
import models.User;
import models.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.AccessTokenRepository;
import services.AccessTokenService;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;
import services.MetricService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static utils.Stopwatch.log;

/**
 * @author resamsel
 * @version 19 Oct 2016
 */
@Singleton
public class AccessTokenServiceImpl extends
        AbstractModelService<AccessToken, Long, AccessTokenCriteria> implements AccessTokenService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenServiceImpl.class);

  private final CacheService cache;
  private final AccessTokenRepository accessTokenRepository;
  private final MetricService metricService;

  @Inject
  public AccessTokenServiceImpl(
          Validator validator,
          CacheService cache,
          AuthProvider authProvider,
          AccessTokenRepository accessTokenRepository,
          LogEntryService logEntryService,
          MetricService metricService,
          ActivityActorRef activityActor) {
    super(validator, cache, accessTokenRepository, AccessToken::getCacheKey, logEntryService, authProvider, activityActor);

    this.cache = cache;
    this.accessTokenRepository = accessTokenRepository;
    this.metricService = metricService;
  }

  @Override
  public PagedList<AccessToken> findBy(AccessTokenCriteria criteria) {
    User loggedInUser = authProvider.loggedInUser(criteria.getRequest());
    if (loggedInUser != null && loggedInUser.role != UserRole.Admin) {
      criteria.setUserId(loggedInUser.id);
    }

    return super.findBy(criteria);
  }

  @Override
  protected PagedList<AccessToken> postFind(PagedList<AccessToken> pagedList, Http.Request request) {
    metricService.logEvent(AccessToken.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  @Override
  protected AccessToken postGet(AccessToken accessToken, Http.Request request) {
    metricService.logEvent(AccessToken.class, ActionType.Read);

    return super.postGet(accessToken, request);
  }

  @Override
  public AccessToken byKey(String accessTokenKey, Http.Request request) {
    metricService.logEvent(AccessToken.class, ActionType.Read);

    return log(
            () -> postGet(accessTokenRepository.byKey(accessTokenKey), request),
            LOGGER,
            "byKey"
    );
  }

  @Override
  protected void postCreate(AccessToken t, Http.Request request) {
    super.postCreate(t, request);

    metricService.logEvent(AccessToken.class, ActionType.Create);

    // When user has been created
    cache.removeByPrefix("accessToken:criteria:");

    activityActor.tell(
            new ActivityProtocol.Activity<>(
                    ActionType.Create,
                    authProvider.loggedInUser(request),
                    null,
                    dto.AccessToken.class,
                    null,
                    AccessTokenMapper.toDto(t)
            ),
            null
    );
  }

  @Override
  protected void preUpdate(AccessToken t, Http.Request request) {
    super.preUpdate(t, request);

    if (StringUtils.isBlank(t.key)) {
      t.key = generateKey(AccessToken.KEY_LENGTH);
    }

    activityActor.tell(
            new ActivityProtocol.Activity<>(
                    ActionType.Update,
                    authProvider.loggedInUser(request),
                    null,
                    dto.AccessToken.class,
                    AccessTokenMapper.toDto(modelRepository.byId(GetCriteria.from(t.id, request))),
                    AccessTokenMapper.toDto(t)
            ),
            null
    );
  }

  @Override
  protected void preSave(AccessToken t, Http.Request request) {
    super.preSave(t, request);

    User loggedInUser = authProvider.loggedInUser(request);
    if (t.user == null || t.user.id == null
            || (loggedInUser != null && t.user.id != loggedInUser.id && loggedInUser.role != UserRole.Admin)) {
      // only allow admins to create access tokens for other users
      t.user = loggedInUser;
    }
  }

  @Override
  protected AccessToken postUpdate(AccessToken t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(AccessToken.class, ActionType.Update);

    // When user has been updated, the user cache needs to be invalidated
    cache.removeByPrefix("accessToken:criteria:" + t.user.id);

    return t;
  }

  @Override
  protected void postDelete(AccessToken t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(AccessToken.class, ActionType.Delete);

    // When locale has been deleted, the locale cache needs to be invalidated
    cache.removeByPrefix("accessToken:criteria:");
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
