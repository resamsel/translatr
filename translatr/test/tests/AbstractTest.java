package tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.mockito.Mockito;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
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
  protected User createUser(String name, String email) {
    return userService.create(new User().withName(name).withEmail(email).withActive(true));
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void startPlay() {
    super.startPlay();

    cleanDatabase();
  }

  private void cleanDatabase() {
    Transaction tran = Ebean.beginTransaction();
    try {
      Connection conn = tran.getConnection();
      conn.createStatement().executeUpdate("truncate user_ cascade");
      Ebean.commitTransaction();
    } catch (SQLException e) {
      // LOGGER.error("Error while ...", e);
    } finally {
      Ebean.endTransaction();
    }
    Ebean.getServerCacheManager().clearAll();
  }
}
