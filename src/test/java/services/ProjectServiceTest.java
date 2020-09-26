package services;

import actors.ActivityActorRef;
import criterias.GetCriteria;
import criterias.PagedListFactory;
import criterias.ProjectCriteria;
import io.ebean.PagedList;
import mappers.ProjectMapper;
import models.Project;
import models.User;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.MessageRepository;
import repositories.ProjectRepository;
import services.impl.CacheServiceImpl;
import services.impl.NoCacheServiceImpl;
import services.impl.ProjectServiceImpl;
import utils.UserRepositoryMock;
import validators.ProjectNameUniqueChecker;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static assertions.CustomAssertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.ProjectRepositoryMock.createProject;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceTest.class);

  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private Validator validator;
  @Mock
  private ProjectMapper projectMapper;

  private ProjectService target;

  private User johnSmith;
  private User janeDoe;

  @Before
  public void before() {
    projectRepository = mock(
            ProjectRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );
    target = new ProjectServiceImpl(
            validator,
            new NoCacheServiceImpl(),
            projectRepository,
            mock(LocaleService.class),
            mock(KeyService.class),
            mock(MessageService.class),
            mock(MessageRepository.class),
            mock(ProjectUserService.class),
            mock(LogEntryService.class),
            mock(AuthProvider.class),
            mock(MetricService.class),
            mock(ActivityActorRef.class),
            projectMapper,
            mock(PermissionService.class)
    );

    johnSmith = UserRepositoryMock.byUsername("johnsmith");
    janeDoe = UserRepositoryMock.byUsername("janedoe");
  }

  @Test
  public void byId() {
    // given
    Project project = createProject(UUID.randomUUID(), "name", "johnsmith");
    Http.Request request = mock(Http.Request.class);

    when(projectRepository.byId(any(GetCriteria.class))).thenReturn(project);

    // when
    Project actual = target.byId(project.id, request);

    // then
    assertThat(actual).nameIsEqualTo("name");
  }

  @Test
  public void findBy() {
    // given
    Project project = createProject(UUID.randomUUID(), "name", johnSmith.username);
    Http.Request request = mock(Http.Request.class);
    ProjectCriteria criteria = new ProjectCriteria().withSearch("name");

    when(projectRepository.findBy(eq(criteria))).thenReturn(PagedListFactory.create(project));

    // when
    PagedList<Project> actual = target.findBy(criteria);

    // then
    assertThat(actual.getList().get(0)).nameIsEqualTo("name");
  }

  @Test
  public void byOwnerAndName() {
    // given
    Project project = createProject(UUID.randomUUID(), "name", johnSmith.username);
    Http.Request request = mock(Http.Request.class);

    when(projectRepository.byOwnerAndName(johnSmith.username, project.name, null)).thenReturn(project);

    // when
    Project actual = target.byOwnerAndName(project.owner.username, project.name, request);

    // then
    assertThat(actual).nameIsEqualTo("name");
  }

  @Test
  public void increaseWordCountBy() {
    // given
    Project project = createProject(UUID.randomUUID(), "name", johnSmith.username);
    Http.Request request = mock(Http.Request.class);

    when(projectRepository.byId(any(GetCriteria.class))).thenReturn(project);

    // when
    Project actual = target.increaseWordCountBy(project.id, 1, request);

    // then
    assertThat(actual).nameIsEqualTo("name").wordCountIsEqualTo(1);
  }

  @Test
  public void resetWordCount() {
    // given
    Project project = createProject(UUID.randomUUID(), "name", johnSmith.username);
    project.wordCount = 100;
    Http.Request request = mock(Http.Request.class);

    when(projectRepository.byId(any(GetCriteria.class))).thenReturn(project);

    // when
    Project actual = target.resetWordCount(project.id, request);

    // then
    assertThat(actual).wordCountIsNull();
  }

  @Test
  public void changeOwner() {
    // given
    Project projectJohn = createProject(UUID.randomUUID(), "name", johnSmith.username);
    Project projectJane = createProject(UUID.randomUUID(), "name", janeDoe.username);
    dto.Project projectJohnDto = new dto.Project();
    projectJohnDto.name = projectJohn.name;
    Http.Request request = mock(Http.Request.class);

    when(projectRepository.byId(any(GetCriteria.class))).thenReturn(projectJohn);
    when(projectMapper.toDto(eq(projectJohn), eq(request))).thenReturn(projectJohnDto);
    when(projectRepository.update(eq(projectJohn))).thenReturn(projectJohn);

    // when
    Project actual = target.changeOwner(projectJohn, janeDoe, request);

    // then
    assertThat(actual).ownerNameIsEqualTo(janeDoe.name);
  }
}
