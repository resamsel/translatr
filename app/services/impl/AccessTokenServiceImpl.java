package services.impl;

import io.ebean.PagedList;
import criterias.AccessTokenCriteria;
import models.AccessToken;
import models.ActionType;
import models.User;
import models.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.AccessTokenRepository;
import services.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

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
          Validator validator, CacheService cache, AuthProvider authProvider,
          AccessTokenRepository accessTokenRepository, LogEntryService logEntryService,
          MetricService metricService) {
    super(validator, cache, accessTokenRepository, AccessToken::getCacheKey, logEntryService, authProvider);

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

  /**
   * {@inheritDoc}
   */
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
  }

  @Override
  protected AccessToken postUpdate(AccessToken t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(AccessToken.class, ActionType.Update);

    // When user has been updated, the user cache needs to be invalidated
    cache.removeByPrefix("accessToken:criteria:" + t.user.id);

    return t;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(AccessToken t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(AccessToken.class, ActionType.Delete);

    // When locale has been deleted, the locale cache needs to be invalidated
    cache.removeByPrefix("accessToken:criteria:");
  }
}
