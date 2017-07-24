package integration.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import assertions.ResultAssert;
import controllers.Projects;
import controllers.routes;
import java.util.UUID;
import models.User;
import org.joda.time.DateTime;
import org.junit.Test;
import play.cache.CacheApi;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;
import utils.TestFactory;
import utils.UserRepository;

/**
 * Created by resamsel on 10/07/2017.
 */
public class UsersTest extends ControllerTest {

  @Test
  public void testIndexDenied() {
    assertAccessDenied(routes.Users.index(), "Users overview denied");
  }

  @Test
  public void testIndex() {
    Result result = Helpers.route(app,
        new RequestBuilder()
            .session(TestFactory.createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Users.index().url()));

    ResultAssert.assertThat(result)
        .as("Users overview")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Test
  public void testUserDenied() {
    assertAccessDenied(routes.Users.user("johnsmith"), "User view denied");
  }

  @Test
  public void testUser() {
    Result result = Helpers.route(app,
        new RequestBuilder()
            .session(TestFactory.createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Users.user("johnsmith").url()));

    ResultAssert.assertThat(result)
        .as("User view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Override
  protected void prepareCache(CacheApi cache) {
    super.prepareCache(cache);

    User johnSmith = UserRepository.byUsername("johnsmith");
    when(cache.getOrElse(eq("google:123916278356185"), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
    when(cache.getOrElse(eq("username:johnsmith"), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
  }
}
