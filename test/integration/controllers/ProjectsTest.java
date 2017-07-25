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
import static utils.TestFactory.createSession;

import controllers.Projects;
import controllers.routes;
import models.Project;
import models.User;
import org.junit.Test;
import play.cache.CacheApi;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;
import utils.ProjectRepository;
import utils.UserRepository;

/**
 * Created by resamsel on 10/07/2017.
 */
public class ProjectsTest extends ControllerTest {

  @Test
  public void testIndexDenied() {
    assertAccessDenied(routes.Projects.index("", "", 20, 0), "Projects overview denied");
  }

  @Test
  public void testIndex() {
    Result result = Helpers.route(app,
        new RequestBuilder()
            .session(createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Projects.index("", "", 20, 0).url()));

    assertThat(result)
        .as("projects overview")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("johnsmith@google.com", mat);
  }

  @Test
  public void testProjectDenied() {
    assertAccessDenied(routes.Projects.projectBy("johnsmith", "project1"), "Project view denied");
  }

  @Test
  public void testProject() {
    Result result = Helpers.route(app,
        new RequestBuilder()
            .session(createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Projects.projectBy("johnsmith", "project1").url()));

    assertThat(result)
        .as("project view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains("project1", mat);
  }

  @Test
  public void testLocalesDenied() {
    assertAccessDenied(routes.Projects
        .localesBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "Project locales denied");
  }

  @Test
  public void testLocales() {
    Result result = Helpers.route(app,
        new RequestBuilder()
            .session(createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Projects
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
            DEFAULT_OFFSET), "Project keys denied");
  }

  @Test
  public void testKeys() {
    Result result = Helpers.route(app,
        new RequestBuilder()
            .session(createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Projects
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
            DEFAULT_OFFSET), "Project members denied");
  }

  @Test
  public void testMembers() {
    Result result = Helpers.route(app,
        new RequestBuilder()
            .session(createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Projects
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
  public void testActivityDenied() {
    assertAccessDenied(routes.Projects
        .activityBy("johnsmith", "project1", DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "Project activity denied");
  }

  @Test
  public void testActivity() {
    Result result = Helpers.route(app,
        new RequestBuilder()
            .session(createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Projects
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

    User johnSmith = UserRepository.byUsername("johnsmith");
    Project project1 = ProjectRepository.byOwnerAndName("johnsmith", "project1");
    when(cache.getOrElse(eq("google:123916278356185"), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
    when(cache.getOrElse(eq("project:owner:johnsmith:name:project1"), any(), anyInt()))
        .thenAnswer(a -> project1);
  }
}
