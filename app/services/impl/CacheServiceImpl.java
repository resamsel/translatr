package services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.SyncCacheApi;
import services.CacheService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedMap;

@Singleton
public class CacheServiceImpl implements CacheService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheServiceImpl.class);

  private final SyncCacheApi cache;

  private final Map<String, Integer> cacheKeys = synchronizedMap(new HashMap<>());

  @Inject
  public CacheServiceImpl(SyncCacheApi cache) {
    this.cache = cache;
  }

  @Override
  public <T> Optional<T> get(String key) {
    return cache.get(key);
  }

  @Override
  public <T> Optional<T> getOptional(String key) {
    return cache.get(key);
  }

  @Override
  public <T> T getOrElseUpdate(String key, Callable<T> block, int expiration) {
    LOGGER.debug("getOrElseUpdate(key={}, block=..., expiration={})(cache={})", key, expiration,
            cache.getClass());

    cacheKeys.put(key, expiration);
    return cache.getOrElseUpdate(key, block, expiration);
  }

  @Override
  public <T> T getOrElseUpdate(String key, Callable<T> block) {
    cacheKeys.put(key, -1);
    return cache.getOrElseUpdate(key, block);
  }

  @Override
  public void set(String key, Object value, int expiration) {
    cacheKeys.put(key, expiration);
    cache.set(key, value, expiration);
  }

  @Override
  public void set(String key, Object value) {
    cacheKeys.put(key, -1);
    cache.set(key, value);
  }

  @Override
  public void remove(String key) {
    cacheKeys.remove(key);
    cache.remove(key);
  }

  @Override
  public Map<String, Integer> keys() {
    return new HashMap<>(cacheKeys);
  }

  @Override
  public void removeByPrefix(String prefix) {
    LOGGER.debug("removeByPrefix(prefix={})", prefix);

    if (prefix != null) {
      removeAll(key -> key != null && key.startsWith(prefix));
    }
  }

  @Override
  public void removeAll(Predicate<String> filter) {
    synchronized (cacheKeys) {
      List<String> keys = cacheKeys.keySet().stream().filter(filter)
              .collect(Collectors.toList());

      LOGGER
              .debug("removeAll: found keys for given filter: {} (all: {})", keys, cacheKeys.keySet());

      keys.forEach(key -> {
        LOGGER.debug("Removing key {}", key);
        cacheKeys.remove(key);
        cache.remove(key);
      });
    }
  }

  @Override
  public void removeAll() {
    removeAll(key -> true);
  }
}
