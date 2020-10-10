package modules;

import com.google.inject.AbstractModule;
import utils.ApplicationStart;

public class StartModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ApplicationStart.class).asEagerSingleton();
  }
}
