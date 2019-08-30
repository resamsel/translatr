package integration.controllers;

import assertions.ProjectAssert;
import com.google.common.collect.ImmutableMap;
import controllers.Projects;
import controllers.routes;
import criterias.PagedListFactory;
import criterias.ProjectCriteria;
import models.Project;
import models.ProjectRole;
import models.User;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.Binding;
import play.cache.CacheApi;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;
import services.ProjectService;
import services.ProjectUserService;

import java.util.Collections;

import static assertions.ResultAssert.assertThat;
import static controllers.AbstractController.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static utils.ProjectRepositoryMock.byOwnerAndName;
import static utils.ProjectUserRepositoryMock.by;
import static utils.TestFactory.requestAsJohnSmith;
import static utils.UserRepositoryMock.byUsername;

/**
 * Created by resamsel on 10/07/2017.
 */
public class ProjectsTest extends ControllerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsTest.class);

  private ProjectService projectService;
  private ProjectUserService projectUserService;

  private User johnSmith;
  private User janeDoe;
  private Project project1;

  @Test
  public void testIndexDenied() {
    assertAccessDenied(Projects.indexRoute(), "projects overview denied");
  }

  @Test
  public void testIndex() {
    Result result = Helpers
        .route(app, requestAsJohnSmith().uri(Projects.indexRoute().url()));

    assertThat(result)
        .as("projects overview")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(johnSmith.email, mat);
  }

  @Test
  public void testProjectDenied() {
    assertAccessDenied(routes.Projects.projectBy(johnSmith.username, project1.name),
        "project view denied");
  }

  @Test
  public void testProject() {
    Result result = Helpers.route(app,
        requestAsJohnSmith()
            .uri(routes.Projects.projectBy(johnSmith.username, project1.name).url()));

    assertThat(result)
        .as("project view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(project1.name, mat)
        .contentContains(byOwnerAndName(johnSmith.username, project1.name).id.toString(), mat);
  }

  @Test
  public void testEditDenied() {
    assertAccessDenied(routes.Projects.editBy(johnSmith.username, project1.name),
        "project edit denied");
  }

  @Test
  public void testEdit() {
    Result result = Helpers.route(app,
        requestAsJohnSmith().uri(routes.Projects.editBy(johnSmith.username, project1.name).url()));

    assertThat(result)
        .as("project edit view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .contentContains(project1.name, mat);
  }

  @Test
  public void testDoEditDenied() {
    assertAccessDenied(routes.Projects.doEditBy(johnSmith.username, project1.name),
        "project do edit denied");
  }

  @Test
  public void testDoEdit() {
    Result result = Helpers.route(
        app,
        requestAsJohnSmith()
            .method("POST")
            .bodyForm(ImmutableMap.of("name", "Changed-Name"))
            .uri(routes.Projects.doEditBy(johnSmith.username, project1.name).url())
    );

    assertThat(result)
        .as("project do edit view")
        .statusIsEqualTo(Projects.SEE_OTHER)
        .headerIsEqualTo("Location",
            routes.Projects.projectBy(johnSmith.username, project1.name).url());
  }

  @Test
  public void testLocalesDenied() {
    assertAccessDenied(routes.Projects
        .localesBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "project locales denied");
  }

  @Test
  public void testLocales() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Projects
        .localesBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET).url()));

    assertThat(result)
        .as("project locales view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(project1.name, mat);
  }

  @Test
  public void testKeysDenied() {
    assertAccessDenied(routes.Projects
        .keysBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "project keys denied");
  }

  @Test
  public void testKeys() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Projects
        .keysBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET).url()));

    assertThat(result)
        .as("project keys view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(project1.name, mat);
  }

  @Test
  public void testMembersDenied() {
    assertAccessDenied(routes.Projects
        .membersBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "project members denied");
  }

  @Test
  public void testMembers() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Projects
        .membersBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET).url()));

    assertThat(result)
        .as("project members view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(project1.name, mat);
  }

  @Test
  public void testMemberAddDenied() {
    assertAccessDenied(routes.Projects
        .memberAddBy(johnSmith.username, project1.name), "project member add denied");
  }

  @Test
  public void testMemberAdd() {
    Result result = Helpers.route(app,
        requestAsJohnSmith()
            .uri(routes.Projects.memberAddBy(johnSmith.username, project1.name).url()));

    assertThat(result)
        .as("project member add view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(project1.name, mat);
  }

  @Test
  public void testDoMemberAddDenied() {
    assertAccessDenied(routes.Projects.doMemberAddBy(johnSmith.username, project1.name),
        "project member do add denied");
  }

  @Test
  public void testDoMemberAdd() {
    Result result = Helpers.route(app,
        requestAsJohnSmith()
            .method("POST")
            .bodyForm(ImmutableMap.of("username", janeDoe.username, "role", "Manager"))
            .uri(routes.Projects.doMemberAddBy(johnSmith.username, project1.name).url()));

    assertThat(result)
        .as("project member do add view")
        .statusIsEqualTo(Projects.SEE_OTHER)
        .headerIsEqualTo("location", routes.Projects
            .membersBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER,
                DEFAULT_LIMIT,
                DEFAULT_OFFSET).url());

    spy(projectUserService).create(any());
  }

  @Test
  public void testDoOwnerChangeDenied() {
    assertAccessDenied(
        new RequestBuilder()
            .method("POST")
            .uri(routes.Projects.doOwnerChangeBy(johnSmith.username, project1.name).url()),
        "project owner change denied");
  }

  @Test
  public void testDoOwnerChangeDuplicateName() {
    Result result = Helpers.route(app,
        requestAsJohnSmith()
            .method("POST")
            .bodyForm(ImmutableMap.of(
                "projectId", project1.id.toString(),
                "ownerId", janeDoe.id.toString(),
                "projectName", project1.name
            ))
            .uri(routes.Projects.doOwnerChangeBy(johnSmith.username, project1.name).url()));

    assertThat(result)
        .as("project do owner change view")
        .statusIsEqualTo(Projects.BAD_REQUEST);

    ProjectAssert.assertThat(project1).ownerIsEqualTo(johnSmith);

    verify(projectService, never()).create(any());
  }

  @Test
  public void testDoOwnerChange() {
    Result result = Helpers.route(app,
        requestAsJohnSmith()
            .method("POST")
            .bodyForm(ImmutableMap.of(
                "projectId", project1.id.toString(),
                "ownerId", janeDoe.id.toString(),
                "projectName", "project2"
            ))
            .uri(routes.Projects.doOwnerChangeBy(johnSmith.username, project1.name).url()));

    assertThat(result)
        .as("project do owner change view")
        .statusIsEqualTo(Projects.SEE_OTHER)
        .headerIsEqualTo("location", routes.Projects
            .membersBy(janeDoe.username, "project2", DEFAULT_SEARCH, DEFAULT_ORDER,
                DEFAULT_LIMIT, DEFAULT_OFFSET).url());

    ProjectAssert.assertThat(project1).ownerIsEqualTo(janeDoe);

    spy(projectService).create(any());

    // reset owner
    project1.owner = johnSmith;
  }

  @Test
  public void testActivityDenied() {
    assertAccessDenied(routes.Projects
        .activityBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET), "project activity denied");
  }

  @Test
  public void testActivity() {
    Result result = Helpers.route(app, requestAsJohnSmith().uri(routes.Projects
        .activityBy(johnSmith.username, project1.name, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
            DEFAULT_OFFSET).url()));

    assertThat(result)
        .as("project activity view")
        .statusIsEqualTo(Projects.OK)
        .contentTypeIsEqualTo("text/html")
        .charsetIsEqualTo("utf-8")
        .contentContains(project1.name, mat);
  }

  @Override
  protected Binding<?>[] createBindings() {
    johnSmith = byUsername("johnsmith");
    janeDoe = byUsername("janedoe");
    project1 = byOwnerAndName(johnSmith.username, "project1");

    projectService = mock(
        ProjectService.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );
    projectUserService = mock(ProjectUserService.class);

    when(projectService.findBy(any()))
        .thenAnswer(a -> PagedListFactory.create(project1));
    when(projectService.findBy(
        eq(new ProjectCriteria().withOwnerId(janeDoe.id).withName("project2"))))
        .thenAnswer(a -> PagedListFactory.create(Collections.emptyList()));
    when(projectService.byOwnerAndName(eq(johnSmith.username), eq(project1.name)))
        .thenAnswer(a -> project1);
    when(projectService.create(any()))
        .thenAnswer(a -> a.getArguments()[0]);
    when(projectService.update(any()))
        .thenAnswer(a -> a.getArguments()[0]);
    doAnswer(a -> {
      Project p = a.getArgument(0);

      p.owner = a.getArgument(1);

      return a;
    }).when(projectService).changeOwner(any(), any());
    when(projectUserService.findBy(any()))
        .thenAnswer(a -> PagedListFactory.create(
            by(project1, johnSmith, ProjectRole.Owner),
            by(project1, janeDoe, ProjectRole.Manager)));
    when(projectUserService.create(any()))
        .thenAnswer(a -> a.getArguments()[0]);

    return ArrayUtils.addAll(
        super.createBindings(),
        bind(ProjectService.class).toInstance(projectService),
        bind(ProjectUserService.class).toInstance(projectUserService)
    );
  }

  @Override
  protected void prepareCache(CacheApi cache) {
    super.prepareCache(cache);

    String project1CacheKey = Project.getCacheKey(johnSmith.username, project1.name);
    String johnSmithCacheKey = "google:123916278356185";
    String janeDoeCacheKey = User.getCacheKey(janeDoe.username);
    String janeDoeIdCacheKey = User.getCacheKey(janeDoe.id);

    when(cache.getOrElse(eq(johnSmithCacheKey), any(), anyInt()))
        .thenAnswer(a -> johnSmith);
    when(cache.getOrElse(eq(project1CacheKey), any(), anyInt()))
        .thenAnswer(a -> project1);
    when(cache.getOrElse(eq(janeDoeCacheKey), any(), anyInt()))
        .thenAnswer(a -> janeDoe);
    when(cache.getOrElse(eq(janeDoeIdCacheKey), any(), anyInt()))
        .thenAnswer(a -> janeDoe);
  }
}
