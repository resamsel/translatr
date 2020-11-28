package modules;

import auth.CustomCookieSessionStore;
import com.google.inject.AbstractModule;
import org.pac4j.play.store.PlaySessionStore;

public class CookieSessionStoreModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PlaySessionStore.class).to(CustomCookieSessionStore.class);
  }
}
