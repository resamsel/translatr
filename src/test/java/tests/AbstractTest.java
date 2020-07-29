package tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

import java.util.concurrent.Callable;
import models.User;
import org.mockito.Mockito;
import play.Application;
import play.api.cache.EhCacheModule;
import play.api.inject.Binding;
import play.cache.CacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import services.AuthProvider;
import services.UserService;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class AbstractTest extends WithApplication {

  protected UserService userService;
  protected CacheApi cache;
  protected AuthProvider authProvider;

  protected User createUser(String name, String email) {
    return userService.create(new User().withName(name).withEmail(email).withActive(true));
  }

  private GuiceApplicationBuilder builder() {
    return new GuiceApplicationBuilder()
        .disable(EhCacheModule.class)
        .overrides(createBindings());
  }

  protected Binding<?>[] createBindings() {
    cache = Mockito.mock(CacheApi.class);
    authProvider = Mockito.mock(AuthProvider.class);

    prepareCache(cache);

    return new Binding[]{
        bind(CacheApi.class).toInstance(cache),
        bind(AuthProvider.class).toInstance(authProvider)
    };
  }

  protected void prepareCache(CacheApi cache) {
    when(cache.getOrElse(anyString(), any(), anyInt()))
        .thenAnswer(invocation -> ((Callable<?>) invocation.getArguments()[1]).call());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Application provideApplication() {
    GuiceApplicationBuilder builder = builder();

    return builder.build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startPlay() {
    super.startPlay();

    userService = app.injector().instanceOf(UserService.class);

    injectMembers();
  }

  /**
   *
   */
  protected void injectMembers() {
  }
}
