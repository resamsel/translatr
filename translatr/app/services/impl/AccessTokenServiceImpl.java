package services.impl;

import static utils.Stopwatch.log;

import java.util.Base64;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import models.AccessToken;
import models.ActionType;
import models.LogEntry;
import play.Configuration;
import play.cache.CacheApi;
import services.AccessTokenService;
import services.LogEntryService;

/**
 *
 * @author resamsel
 * @version 19 Oct 2016
 */
@Singleton
public class AccessTokenServiceImpl extends AbstractModelService<AccessToken, dto.AccessToken>
    implements AccessTokenService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenServiceImpl.class);

  private final CacheApi cache;

  /**
   * @param configuration
   */
  @Inject
  public AccessTokenServiceImpl(Configuration configuration, LogEntryService logEntryService,
      CacheApi cache) {
    super(dto.AccessToken.class, configuration, logEntryService);
    this.cache = cache;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected AccessToken byId(JsonNode id) {
    return AccessToken.byId(id.asLong());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected AccessToken toModel(dto.AccessToken dto) {
    return dto.toModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AccessToken getByKey(String accessTokenKey) {
    return log(() -> cache.getOrElse(getCacheKey(accessTokenKey),
        () -> AccessToken.byKeyUncached(accessTokenKey), 60), LOGGER, "getByKey");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(AccessToken t, boolean update) {
    if (t.key == null)
      t.key = generateKey(AccessToken.KEY_LENGTH);

    if (update)
      logEntryService.save(LogEntry.from(ActionType.Update, null, dto.AccessToken.class,
          dto.AccessToken.from(AccessToken.byId(t.id)), dto.AccessToken.from(t)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(AccessToken t, boolean update) {
    if (!update)
      logEntryService.save(LogEntry.from(ActionType.Create, null, dto.AccessToken.class, null,
          dto.AccessToken.from(t)));

    cache.remove(getCacheKey(t.key));
  }

  /**
   * @param length
   * @return
   */
  private String generateKey(int length) {
    String raw = Base64.getEncoder()
        .encodeToString(String.format("%s%s", UUID.randomUUID(), UUID.randomUUID()).getBytes());

    if (raw.length() > length)
      raw = raw.substring(0, length);

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
