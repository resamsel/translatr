package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.SyncCacheApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class CacheApiMock implements SyncCacheApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheApiMock.class);

  private final Map<String, Object> map = new HashMap<>();

  @Override
  public <T> T get(String key) {
    LOGGER.debug("get(key={})", key);

    T value = (T) map.get(key);

    LOGGER.debug("returning {}", value);

    return value;
  }

  @Override
  public <T> Optional<T> getOptional(String key) {
    return Optional.ofNullable(get(key));
  }

  @Override
  public <T> T getOrElseUpdate(String key, Callable<T> block, int expiration) {
    LOGGER.debug("getOrElseUpdate(key={}, block=..., expiration={})", key, expiration);

    if (!map.containsKey(key)) {
      try {
        map.put(key, block.call());
      } catch (Exception e) {
        // do nothing
        LOGGER.warn("Error while calling block", e);
      }
    }
    return get(key);
  }

  @Override
  public <T> T getOrElseUpdate(String key, Callable<T> block) {
    LOGGER.debug("getOrElseUpdate(key={}, block=...)", key);

    return getOrElseUpdate(key, block, -1);
  }

  @Override
  public void set(String key, Object value, int expiration) {
    LOGGER.debug("set(key={}, value={}, expiration={})", key, value, expiration);

    map.put(key, value);
  }

  @Override
  public void set(String key, Object value) {
    LOGGER.debug("set(key={}, value={})", key, value);

    set(key, value, -1);
  }

  @Override
  public void remove(String key) {
    LOGGER.debug("remove(key={})", key);

    map.remove(key);
  }
}
