package services.impl;

import play.cache.CacheApi;
import services.CacheService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Callable;

@Singleton
public class NoCacheServiceImpl extends CacheServiceImpl implements CacheService {
  @Inject
  public NoCacheServiceImpl(CacheApi cache) {
    super(cache);
  }

  @Override
  public <T> T getOrElse(String key, Callable<T> block) {
    try {
      return block.call();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public <T> T getOrElse(String key, Callable<T> block, int expiration) {
    try {
      return block.call();
    } catch (Exception e) {
      return null;
    }
  }


}
