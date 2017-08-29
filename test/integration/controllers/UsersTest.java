package integration.controllers;

import static assertions.ResultAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static utils.TestFactory.requestAsJohnSmith;
import static utils.UserRepositoryMock.byUsername;

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

  private User johnSmith;
  private User janeDoe;

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
        .contentContains(johnSmith.email, mat);
  }

  @Test
  public void testUserDenied() {
    assertAccessDenied(routes.Users.user(johnSmith.username), "user view denied");
  }

  @Test
  public void testUser() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.user(johnSmith.username).url()));

    assertThat(result)
        .as("user view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("activity", mat)
        .contentContains(johnSmith.email, mat);
  }

  @Test
  public void testUserJaneDoe() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.user(janeDoe.username).url()));

    assertThat(result)
        .as("user janedoe view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(janeDoe.username, mat);
  }

  @Test
  public void testProjectsDenied() {
    assertAccessDenied(routes.Users.projects(johnSmith.username), "user projects view denied");
  }

  @Test
  public void testProjectsJaneDoeDenied() {
    assertAccessDenied(requestAsJohnSmith().uri(routes.Users.projects(janeDoe.username).url()),
        "user projects jane doe denied");
  }

  @Test
  public void testProjects() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.projects(johnSmith.username).url()));

    assertThat(result)
        .as("user projects view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(johnSmith.email, mat);
  }

  @Test
  public void testActivityDenied() {
    assertAccessDenied(routes.Users.activity(johnSmith.username), "user activity view denied");
  }

  @Test
  public void testActivity() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.activity(johnSmith.username).url()));

    assertThat(result)
        .as("user activity view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(johnSmith.email, mat);
  }

  @Test
  public void testLinkedAccountsDenied() {
    assertAccessDenied(routes.Users.linkedAccounts(johnSmith.username),
        "user projects view denied");
  }

  @Test
  public void testLinkedAccounts() {
    Result result = Helpers
        .route(app,
            requestAsJohnSmith().uri(routes.Users.linkedAccounts(johnSmith.username).url()));

    assertThat(result)
        .as("user linked accounts view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(johnSmith.email, mat);
  }

  @Test
  public void testAccessTokensDenied() {
    assertAccessDenied(routes.Users.accessTokens(johnSmith.username), "user projects view denied");
  }

  @Test
  public void testAccessTokens() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Users.accessTokens(johnSmith.username).url()));

    assertThat(result)
        .as("user access tokens view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(johnSmith.email, mat);
  }

  @Override
  protected void prepareCache(CacheApi cache) {
    super.prepareCache(cache);

    johnSmith = byUsername("johnsmith");
    janeDoe = byUsername("janedoe");

    when(cache.getOrElse(eq("google:123916278356185"), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
    when(cache.getOrElse(eq(User.getCacheKey(johnSmith.username)), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
    when(cache.getOrElse(eq(User.getCacheKey(janeDoe.username)), any(), anyInt()))
        .thenAnswer(a -> janeDoe);
  }
}
