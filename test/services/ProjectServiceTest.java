package services;

import static assertions.ProjectAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static utils.ProjectRepositoryMock.createProject;

import criterias.HasNextPagedList;
import criterias.ProjectCriteria;
import java.util.UUID;
import javax.validation.Validator;
import models.Project;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import repositories.ProjectRepository;
import services.impl.CacheServiceImpl;
import services.impl.ProjectServiceImpl;
import utils.CacheApiMock;

public class ProjectServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceTest.class);

  private ProjectRepository projectRepository;
  private ProjectService projectService;
  private CacheService cacheService;

  @Test
  public void testById() {
    // mock project
    Project project = createProject(UUID.randomUUID(), "name", "johnsmith");
    when(projectRepository.byId(eq(project.id))).thenReturn(project);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("project:id:" + project.id);
    assertThat(projectService.byId(project.id)).nameIsEqualTo("name");
    verify(projectRepository, times(1)).byId(eq(project.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("project:id:" + project.id);
    assertThat(projectService.byId(project.id)).nameIsEqualTo("name");
    verify(projectRepository, times(1)).byId(eq(project.id));

    // This should trigger cache invalidation
    project = createProject(project, "name2");
    projectService.update(project);

    when(projectRepository.byId(eq(project.id))).thenReturn(project);
    assertThat(cacheService.keys().keySet()).doesNotContain("project:id:" + project.id);
    assertThat(projectService.byId(project.id)).nameIsEqualTo("name2");
    verify(projectRepository, times(2)).byId(eq(project.id));
  }

  @Test
  public void testFindBy() {
    // mock project
    Project project = createProject(UUID.randomUUID(), "name", "johnsmith");
    ProjectCriteria criteria = new ProjectCriteria().withSearch("name");
    when(projectRepository.findBy(eq(criteria))).thenReturn(HasNextPagedList.create(project));

    // This invocation should feed the cache
    assertThat(projectService.findBy(criteria).getList().get(0))
        .as("uncached")
        .nameIsEqualTo("name");
    verify(projectRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(projectService.findBy(criteria).getList().get(0))
        .as("cached")
        .nameIsEqualTo("name");
    verify(projectRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    project = createProject(project, "name3");
    projectService.update(project);

    when(projectRepository.findBy(eq(criteria))).thenReturn(HasNextPagedList.create(project));
    assertThat(projectService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("name3");
    verify(projectRepository, times(2)).findBy(eq(criteria));
  }

  @Test
  public void testByOwnerAndName() {
    // mock project
    Project project = createProject(UUID.randomUUID(), "name", "johnsmith");
    when(projectRepository.byOwnerAndName(eq(project.owner.username), eq(project.name)))
        .thenReturn(project);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet())
        .doesNotContain("project:owner:" + project.owner.username + ":" + project.name);
    assertThat(projectService.byOwnerAndName(project.owner.username, project.name))
        .nameIsEqualTo("name");
    verify(projectRepository, times(1))
        .byOwnerAndName(eq(project.owner.username), eq(project.name));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet())
        .contains("project:owner:" + project.owner.username + ":" + project.name);
    assertThat(projectService.byOwnerAndName(project.owner.username, project.name))
        .nameIsEqualTo("name");
    verify(projectRepository, times(1))
        .byOwnerAndName(eq(project.owner.username), eq(project.name));

    // This should trigger cache invalidation
    project = createProject(project, "name2");
    projectService.update(project);

    when(projectRepository.byOwnerAndName(eq(project.owner.username), eq(project.name)))
        .thenReturn(project);
    assertThat(cacheService.keys().keySet())
        .doesNotContain("project:owner:" + project.owner.username + ":" + project.name);
    assertThat(projectService.byOwnerAndName(project.owner.username, project.name))
        .nameIsEqualTo("name2");
    verify(projectRepository, times(1))
        .byOwnerAndName(eq(project.owner.username), eq(project.name));
  }

  @Before
  public void before() {
    projectRepository = mock(
        ProjectRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );
    cacheService = new CacheServiceImpl(new CacheApiMock());
    projectService = new ProjectServiceImpl(
        mock(Validator.class),
        cacheService,
        projectRepository,
        mock(LocaleService.class),
        mock(KeyService.class),
        mock(MessageService.class),
        mock(MessageRepository.class),
        mock(LogEntryService.class)
    );

    when(projectRepository.update(any())).thenAnswer(a -> a.getArguments()[0]);
  }
}
