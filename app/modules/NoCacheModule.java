package modules;

import com.google.inject.AbstractModule;
import services.CacheService;
import services.impl.NoCacheServiceImpl;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
public class NoCacheModule extends AbstractModule {

  /**
   * {@inheritDoc}
   */
  @Override
  protected void configure() {
    bind(CacheService.class).to(NoCacheServiceImpl.class);
  }
}
