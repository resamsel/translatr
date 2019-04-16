package services.impl;

import static utils.Stopwatch.log;

import com.avaje.ebean.PagedList;
import criterias.AccessTokenCriteria;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import models.AccessToken;
import models.User;
import models.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.AccessTokenRepository;
import services.AccessTokenService;
import services.CacheService;
import services.LogEntryService;

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

  @Inject
  public AccessTokenServiceImpl(Validator validator, CacheService cache,
                                AccessTokenRepository accessTokenRepository, LogEntryService logEntryService) {
    super(validator, cache, accessTokenRepository, AccessToken::getCacheKey, logEntryService);

    this.cache = cache;
    this.accessTokenRepository = accessTokenRepository;
  }

  @Override
  public PagedList<AccessToken> findBy(AccessTokenCriteria criteria) {
    User loggedInUser = User.loggedInUser();
    if (loggedInUser != null && loggedInUser.role != UserRole.Admin) {
      criteria.setUserId(loggedInUser.id);
    }

    return super.findBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AccessToken byKey(String accessTokenKey) {
    return log(() -> cache.getOrElse(AccessToken.getCacheKey(accessTokenKey),
        () -> accessTokenRepository.byKey(accessTokenKey), 60), LOGGER, "getByKey");
  }

  @Override
  protected void postCreate(AccessToken t) {
    super.postCreate(t);

    // When user has been created
    cache.removeByPrefix("accessToken:criteria:");
  }

  @Override
  protected void postUpdate(AccessToken t) {
    super.postUpdate(t);

    // When user has been updated, the user cache needs to be invalidated
    cache.removeByPrefix("accessToken:criteria:" + t.user.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(AccessToken t) {
    super.postDelete(t);

    // When locale has been deleted, the locale cache needs to be invalidated
    cache.removeByPrefix("accessToken:criteria:");
  }
}
