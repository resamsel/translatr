package services.impl;

import services.CacheService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@Singleton
public class NoCacheServiceImpl implements CacheService {
  @Inject
  public NoCacheServiceImpl() {
  }

  @Override
  public <T> T getOrElseUpdate(String key, Callable<T> block) {
    try {
      return block.call();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void set(String key, Object value, int expiration) {
  }

  @Override
  public void set(String key, Object value) {
  }

  @Override
  public void remove(String key) {
  }

  @Override
  public <T> Optional<T> get(String key) {
    return Optional.empty();
  }

  @Override
  public <T> T getOrElseUpdate(String key, Callable<T> block, int expiration) {
    try {
      return block.call();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Map<String, Integer> keys() {
    return Collections.emptyMap();
  }

  @Override
  public void removeByPrefix(String prefix) {
  }

  @Override
  public void removeAll(Predicate<String> filter) {
  }

  @Override
  public void removeAll() {
  }
}
