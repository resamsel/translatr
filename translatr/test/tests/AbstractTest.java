package tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.mockito.Mockito;

import com.google.inject.Guice;

import models.User;
import play.Application;
import play.api.cache.EhCacheModule;
import play.cache.CacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import services.UserService;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class AbstractTest extends WithApplication {
  @Inject
  protected UserService userService;

  /**
   * @param name
   * @return
   */
  protected User createUser(String name) {
    return userService.create(new User().withName(name));
  }

  protected GuiceApplicationBuilder builder() {
    CacheApi cache = Mockito.mock(CacheApi.class);
    when(cache.getOrElse(anyString(), any(), anyInt()))
        .thenAnswer(invocation -> ((Callable<?>) invocation.getArguments()[1]).call());

    return new GuiceApplicationBuilder().disable(EhCacheModule.class)
        .overrides(bind(CacheApi.class).toInstance(cache));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Application provideApplication() {
    GuiceApplicationBuilder builder = builder();

    Guice.createInjector(builder.applicationModule()).injectMembers(this);

    return builder.build();
  }
}
