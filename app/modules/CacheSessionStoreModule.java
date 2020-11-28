package modules;

import com.google.inject.AbstractModule;
import org.pac4j.play.store.PlayEhCacheSessionStore;
import org.pac4j.play.store.PlaySessionStore;

public class CacheSessionStoreModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PlaySessionStore.class).to(PlayEhCacheSessionStore.class);
  }
}
