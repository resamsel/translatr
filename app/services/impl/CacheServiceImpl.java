package services.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.cache.CacheApi;
import services.CacheService;

@Singleton
public class CacheServiceImpl implements CacheService {

  private final CacheApi cache;

  private final Map<String, Integer> cacheKeys = new HashMap<>();

  @Inject
  public CacheServiceImpl(CacheApi cache) {
    this.cache = cache;
  }

  @Override
  public <T> T get(String key) {
    return cache.get(key);
  }

  @Override
  public <T> T getOrElse(String key, Callable<T> block, int expiration) {
    cacheKeys.put(key, expiration);
    return cache.getOrElse(key, block, expiration);
  }

  @Override
  public <T> T getOrElse(String key, Callable<T> block) {
    cacheKeys.put(key, -1);
    return cache.getOrElse(key, block);
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
    removeAll(key -> key.startsWith(prefix));
  }

  @Override
  public void removeAll(Predicate<String> filter) {
    cacheKeys.keySet().stream().filter(filter).forEach(key -> {
      cacheKeys.remove(key);
      cache.remove(key);
    });
  }

  @Override
  public void removeAll() {
    removeAll(key -> true);
  }
}
