package tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

import java.util.concurrent.Callable;
import javax.inject.Inject;
import models.User;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.api.cache.EhCacheModule;
import play.api.inject.Binding;
import play.cache.CacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import services.UserService;
import utils.TransactionUtils;

/**
 * @author resamsel
 * @version 28 Jan 2017
 */
public class AbstractTest extends WithApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);

  protected UserService userService;

  /**
   * @param name
   * @return
   */
  protected User createUser(String name, String email) {
    return userService.create(new User().withName(name).withEmail(email).withActive(true));
  }

  protected GuiceApplicationBuilder builder() {
    return new GuiceApplicationBuilder().disable(EhCacheModule.class)
        .overrides(createBindings());
  }

  protected Binding<?>[] createBindings() {
    CacheApi cache = Mockito.mock(CacheApi.class);

    prepareCache(cache);

    return new Binding[]{bind(CacheApi.class).toInstance(cache)};
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

    cleanDatabase();
  }

  /**
   *
   */
  protected void injectMembers() {
  }

  private void cleanDatabase() {
    try {
      TransactionUtils.execute(tx -> {
        LOGGER.info("Deleting all users");
        tx.getConnection().createStatement().executeUpdate("truncate user_ cascade");
      });
    } catch (Exception e) {
      LOGGER.error("Error while cleaning database", e);
    }
  }
}
