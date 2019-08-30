package services;

import criterias.PagedListFactory;
import criterias.ProjectCriteria;
import models.Project;
import models.User;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import repositories.ProjectRepository;
import services.impl.CacheServiceImpl;
import services.impl.ProjectServiceImpl;
import utils.CacheApiMock;
import utils.UserRepositoryMock;
import validators.ProjectNameUniqueChecker;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static assertions.ProjectAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.ProjectRepositoryMock.createProject;

public class ProjectServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceTest.class);

  private ProjectRepository projectRepository;
  private ProjectService projectService;
  private CacheService cacheService;
  private User johnSmith;
  private User janeDoe;
  private Validator validator;

  @Test
  public void testById() {
    // mock project
    Project project = createProject(UUID.randomUUID(), "name", "johnsmith");
    projectRepository.create(project);

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

    assertThat(cacheService.keys().keySet()).contains("project:id:" + project.id);
    assertThat(projectService.byId(project.id)).nameIsEqualTo("name2");
    verify(projectRepository, times(1)).byId(eq(project.id));
  }

  @Test
  public void testFindBy() {
    // mock project
    Project project = createProject(UUID.randomUUID(), "name", johnSmith.username);
    projectRepository.create(project);

    // This invocation should feed the cache
    ProjectCriteria criteria = new ProjectCriteria().withSearch("name");
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

    assertThat(projectService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("name3");
    verify(projectRepository, times(2)).findBy(eq(criteria));
  }

  @Test
  public void testByOwnerAndName() {
    // mock project
    Project project = createProject(UUID.randomUUID(), "name", johnSmith.username);
    projectRepository.create(project);

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

    assertThat(cacheService.keys().keySet())
        .doesNotContain("project:owner:" + project.owner.username + ":" + project.name);
    assertThat(projectService.byOwnerAndName(project.owner.username, project.name))
        .nameIsEqualTo("name2");
    verify(projectRepository, times(1))
        .byOwnerAndName(eq(project.owner.username), eq(project.name));
  }

  @Test
  public void testIncreaseWordCountBy() {
    // mock project
    Project project = createProject(UUID.randomUUID(), "name", johnSmith.username);
    projectRepository.create(project);

    assertThat(projectService.byId(project.id)).wordCountIsNull();

    // This should trigger cache invalidation
    projectService.increaseWordCountBy(project.id, 1);

    assertThat(projectService.byId(project.id)).wordCountIsEqualTo(1);
    verify(projectRepository, times(1)).byId(eq(project.id));
  }

  @Test
  public void testResetWordCount() {
    // mock project
    Project project = createProject(UUID.randomUUID(), "name", johnSmith.username);
    project.wordCount = 100;
    projectRepository.create(project);

    assertThat(projectService.byId(project.id)).wordCountIsEqualTo(100);

    // This should trigger cache invalidation
    projectService.resetWordCount(project.id);

    assertThat(projectService.byId(project.id)).wordCountIsNull();
    verify(projectRepository, times(1)).byId(eq(project.id));
  }

  @Test
  public void testChangeOwner() {
    // mock projects
    Project projectJohn = createProject(UUID.randomUUID(), "name", johnSmith.username);
    projectRepository.create(projectJohn);
    Project projectJane = createProject(UUID.randomUUID(), "name", janeDoe.username);
    projectRepository.create(projectJane);

    assertThat(projectService.byId(projectJohn.id))
        .ownerIsEqualTo(johnSmith)
        .nameIsEqualTo(projectJohn.name);
    assertThat(projectService.byId(projectJane.id))
        .ownerIsEqualTo(janeDoe)
        .nameIsEqualTo(projectJane.name);

    when(validator.validate(any())).thenAnswer(a -> {
      Project p = a.getArgument(0);

      if (!new ProjectNameUniqueChecker(projectService).isValid(p)) {
        return new HashSet<>(Collections.singletonList(
            ConstraintViolationImpl.forBeanValidation(
                "",
                Collections.emptyMap(),
                "",
                Project.class,
                p,
                null,
                null,
                null,
                null,
                null)
        ));
      }

      return new HashSet<>();
    });

    try {
      projectService.changeOwner(projectJohn, janeDoe);
      failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
    } catch (Exception e) {
      assertThat(e).isInstanceOf(ConstraintViolationException.class);
    }

    // This should trigger cache invalidation
    projectService.changeOwner(projectJohn.withName("name2"), janeDoe);

    assertThat(projectService.byId(projectJohn.id)).ownerIsEqualTo(janeDoe).nameIsEqualTo("name2");
    verify(projectRepository, times(1)).byId(eq(projectJohn.id));
  }


  @Before
  public void before() {
    validator = mock(Validator.class);
    projectRepository = mock(
        ProjectRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );
    cacheService = new CacheServiceImpl(new CacheApiMock());
    projectService = new ProjectServiceImpl(
        validator,
        cacheService,
        projectRepository,
        mock(LocaleService.class),
        mock(KeyService.class),
        mock(MessageService.class),
        mock(MessageRepository.class),
        mock(ProjectUserService.class),
        mock(LogEntryService.class)
    );
    johnSmith = UserRepositoryMock.byUsername("johnsmith");
    janeDoe = UserRepositoryMock.byUsername("janedoe");

    when(projectRepository.create(any())).then(this::persist);
    when(projectRepository.update(any())).then(this::persist);
  }

  private Project persist(InvocationOnMock a) {
    Project p = a.getArgument(0);

    Set<ConstraintViolation<Project>> violations = validator.validate(p);
    if (violations != null && !violations.isEmpty()) {
      throw new ConstraintViolationException("Violations found", violations);
    }

    when(projectRepository.byId(eq(p.id), any())).thenReturn(p);
    when(projectRepository.byOwnerAndName(eq(p.owner.username), eq(p.name))).thenReturn(p);
    when(projectRepository.findBy(any())).thenReturn(PagedListFactory.create(p));
    return p;
  }
}
