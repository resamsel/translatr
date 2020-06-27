package services;

import com.google.inject.ImplementedBy;
import play.cache.CacheApi;
import services.impl.CacheServiceImpl;

import java.util.Map;
import java.util.function.Predicate;

@ImplementedBy(CacheServiceImpl.class)
public interface CacheService extends CacheApi {

  Map<String, Integer> keys();

  void removeByPrefix(String prefix);

  void removeAll(Predicate<String> filter);

  void removeAll();
}
