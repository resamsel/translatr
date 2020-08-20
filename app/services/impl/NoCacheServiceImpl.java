package services.impl;

import play.cache.SyncCacheApi;
import services.CacheService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Callable;

@Singleton
public class NoCacheServiceImpl extends CacheServiceImpl implements CacheService {
  @Inject
  public NoCacheServiceImpl(SyncCacheApi cache) {
    super(cache);
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
  public <T> T getOrElseUpdate(String key, Callable<T> block, int expiration) {
    try {
      return block.call();
    } catch (Exception e) {
      return null;
    }
  }


}
