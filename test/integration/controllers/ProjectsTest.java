package integration.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import assertions.ResultAssert;
import controllers.Projects;
import controllers.routes;
import java.util.UUID;
import models.Project;
import models.User;
import org.joda.time.DateTime;
import org.junit.Test;
import play.cache.CacheApi;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;
import utils.ProjectRepository;
import utils.TestFactory;
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
            .session(TestFactory.createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Projects.index("", "", 20, 0).url()));

    ResultAssert.assertThat(result)
        .as("Projects overview")
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
            .session(TestFactory.createSession("google", "123916278356185", "asdfasdf"))
            .uri(routes.Projects.projectBy("johnsmith", "project1").url()));

    ResultAssert.assertThat(result)
        .as("Project view")
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
