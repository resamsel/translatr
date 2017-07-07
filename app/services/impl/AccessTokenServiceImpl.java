package services.impl;

import static utils.Stopwatch.log;

import com.avaje.ebean.PagedList;
import criterias.AccessTokenCriteria;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.AccessToken;
import models.ActionType;
import models.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import services.AccessTokenService;
import services.LogEntryService;

/**
 * @author resamsel
 * @version 19 Oct 2016
 */
@Singleton
public class AccessTokenServiceImpl extends
    AbstractModelService<AccessToken, Long, AccessTokenCriteria> implements AccessTokenService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenServiceImpl.class);

  private final CacheApi cache;

  @Inject
  public AccessTokenServiceImpl(Validator validator, LogEntryService logEntryService,
      CacheApi cache) {
    super(validator, logEntryService);
    this.cache = cache;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<AccessToken> findBy(AccessTokenCriteria criteria) {
    return AccessToken.findBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AccessToken byId(Long id, String... fetches) {
    return AccessToken.byId(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AccessToken byKey(String accessTokenKey) {
    return log(() -> cache.getOrElse(getCacheKey(accessTokenKey),
        () -> AccessToken.byKey(accessTokenKey), 60), LOGGER, "getByKey");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(AccessToken t, boolean update) {
    if (t.key == null) {
      t.key = generateKey(AccessToken.KEY_LENGTH);
    }

    if (update) {
      logEntryService.save(LogEntry.from(ActionType.Update, null, dto.AccessToken.class,
          dto.AccessToken.from(byId(t.id)), dto.AccessToken.from(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(AccessToken t, boolean update) {
    if (!update) {
      logEntryService.save(LogEntry.from(ActionType.Create, null, dto.AccessToken.class, null,
          dto.AccessToken.from(t)));
    }

    cache.remove(getCacheKey(t.key));
  }

  /**
   * @param length
   * @return
   */
  private String generateKey(int length) {
    String raw = Base64.getEncoder().encodeToString(String
        .format("%s%s", UUID.randomUUID(), UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));

    if (raw.length() > length) {
      raw = raw.substring(0, length);
    }

    return raw.replace("+", "/");
  }

  /**
   * @param accessTokenKey
   * @return
   */
  private String getCacheKey(String accessTokenKey) {
    return String.format("accessToken:%s", accessTokenKey);
  }
}
