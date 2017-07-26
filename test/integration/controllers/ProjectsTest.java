package integration.controllers;

import static assertions.ResultAssert.assertThat;
import static controllers.AbstractController.DEFAULT_LIMIT;
import static controllers.AbstractController.DEFAULT_OFFSET;
import static controllers.AbstractController.DEFAULT_ORDER;
import static controllers.AbstractController.DEFAULT_SEARCH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static utils.ProjectRepository.byOwnerAndName;
import static utils.TestFactory.requestAsJohnSmith;
import static utils.UserRepository.byUsername;

import controllers.Projects;
import controllers.routes;
import models.Project;
import models.User;
import org.junit.Test;
import play.cache.CacheApi;
import play.mvc.Result;
import play.test.Helpers;

/**
 * Created by resamsel on 10/07/2017.
 */
public class ProjectsTest extends ControllerTest {

  @Test
  public void testIndexDenied() {
    assertAccessDenied(routes.Projects.index("", "", 20, 0), "projects overview denied");
  }

  @Test
  public void testIndex() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(routes.Projects.index("", "", 20, 0).url()));

    assertThat(result)
        .as("projects overview")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Test
  public void testProjectDenied() {
    assertAccessDenied(routes.Projects.projectBy("johnsmith", "project1"), "project view denied");
  }

  @Test
  public void testProject() {
    Result result = Helpers.route(app,
        requestAsJohnSmith().uri(routes.Projects.projectBy("johnsmith", "project1").url()));

    assertThat(result)
        .as("project view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("project1", mat)
        .contentContains(byOwnerAndName("johnsmith", "project1").id.toString(), mat);
  }

  @Test
  public void testLocalesDenied() {
    assertAccessDenied(routes.Projects
        .localesBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "project locales denied");
  }

  @Test
  public void testLocales() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Projects
        .localesBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET).url()));

    assertThat(result)
        .as("project locales view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("project1", mat);
  }

  @Test
  public void testKeysDenied() {
    assertAccessDenied(routes.Projects
        .keysBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "project keys denied");
  }

  @Test
  public void testKeys() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Projects
        .keysBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET).url()));

    assertThat(result)
        .as("project keys view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("project1", mat);
  }

  @Test
  public void testMembersDenied() {
    assertAccessDenied(routes.Projects
        .membersBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "project members denied");
  }

  @Test
  public void testMembers() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Projects
        .membersBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET).url()));

    assertThat(result)
        .as("project members view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("project1", mat);
  }

  @Test
  public void testMemberAddDenied() {
    assertAccessDenied(routes.Projects
        .memberAddBy("johnsmith", "project1"), "project member add denied");
  }

  @Test
  public void testMemberAdd() {
    Result result = Helpers.route(app,
        requestAsJohnSmith().uri(routes.Projects.memberAddBy("johnsmith", "project1").url()));

    assertThat(result)
        .as("project member add view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("project1", mat);
  }

  @Test
  public void testActivityDenied() {
    assertAccessDenied(routes.Projects
        .activityBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "project activity denied");
  }

  @Test
  public void testActivity() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Projects
        .activityBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET).url()));

    assertThat(result)
        .as("project activity view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("project1", mat);
  }

  @Override
  protected void prepareCache(CacheApi cache) {
    super.prepareCache(cache);

    User johnSmith = byUsername("johnsmith");
    Project project1 = byOwnerAndName("johnsmith", "project1");
    when(cache.getOrElse(eq("google:123916278356185"), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
    when(cache.getOrElse(eq("project:owner:johnsmith:name:project1"), any(), anyInt()))
        .thenAnswer(a -> project1);
  }
}
