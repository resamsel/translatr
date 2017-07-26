package integration.controllers;

import static assertions.ResultAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static utils.TestFactory.requestAsJohnSmith;
import static utils.UserRepository.byUsername;

import controllers.Projects;
import controllers.routes;
import models.User;
import org.junit.Test;
import play.cache.CacheApi;
import play.mvc.Result;
import play.test.Helpers;

/**
 * Created by resamsel on 10/07/2017.
 */
public class UsersTest extends ControllerTest {

  @Test
  public void testIndexDenied() {
    assertAccessDenied(routes.Users.index(), "users overview denied");
  }

  @Test
  public void testIndex() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Users.index().url()));

    assertThat(result)
        .as("users overview")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Test
  public void testUserDenied() {
    assertAccessDenied(routes.Users.user("johnsmith"), "user view denied");
  }

  @Test
  public void testUser() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.user("johnsmith").url()));

    assertThat(result)
        .as("user view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("activity", mat)
        .contentContains("johnsmith@google.com", mat);
  }

  @Test
  public void testUserJaneDoe() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.user("janedoe").url()));

    assertThat(result)
        .as("user janedoe view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("janedoe", mat);
  }

  @Test
  public void testProjectsDenied() {
    assertAccessDenied(routes.Users.projects("johnsmith"), "user projects view denied");
  }

  @Test
  public void testProjectsJaneDoeDenied() {
    assertAccessDenied(requestAsJohnSmith().uri(routes.Users.projects("janedoe").url()),
        "user projects jane doe denied");
  }

  @Test
  public void testProjects() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.projects("johnsmith").url()));

    assertThat(result)
        .as("user projects view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Test
  public void testActivityDenied() {
    assertAccessDenied(routes.Users.activity("johnsmith"), "user activity view denied");
  }

  @Test
  public void testActivity() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.activity("johnsmith").url()));

    assertThat(result)
        .as("user activity view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Test
  public void testLinkedAccountsDenied() {
    assertAccessDenied(routes.Users.linkedAccounts("johnsmith"), "user projects view denied");
  }

  @Test
  public void testLinkedAccounts() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.linkedAccounts("johnsmith").url()));

    assertThat(result)
        .as("user linked accounts view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Test
  public void testAccessTokensDenied() {
    assertAccessDenied(routes.Users.accessTokens("johnsmith"), "user projects view denied");
  }

  @Test
  public void testAccessTokens() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.accessTokens("johnsmith").url()));

    assertThat(result)
        .as("user access tokens view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Override
  protected void prepareCache(CacheApi cache) {
    super.prepareCache(cache);

    User johnSmith = byUsername("johnsmith");
    User janeDoe = byUsername("janedoe");

    when(cache.getOrElse(eq("google:123916278356185"), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
    when(cache.getOrElse(eq("username:johnsmith"), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
    when(cache.getOrElse(eq("username:janedoe"), any(), anyInt()))
        .thenAnswer(a -> janeDoe);
  }
}
