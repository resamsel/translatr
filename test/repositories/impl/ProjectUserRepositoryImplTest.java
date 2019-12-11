package repositories.impl;

import actors.ActivityActorRef;
import actors.NotificationActorRef;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import criterias.PagedListFactory;
import criterias.ProjectUserCriteria;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import org.fest.assertions.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import repositories.PagedListFactoryProvider;
import repositories.Persistence;
import repositories.RepositoryProvider;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectUserRepositoryImplTest {

  private ProjectUserRepositoryImpl target;
  @Mock
  private Persistence persistence;
  @Mock
  private Validator validator;
  @Mock
  private ActivityActorRef activityActor;
  @Mock
  private NotificationActorRef notificationActor;
  @Mock
  private RepositoryProvider repositoryProvider;
  @Mock
  private Model.Find<Long, ProjectUser> projectUserRepository;
  @Mock
  private Query<ProjectUser> query;
  @Mock
  private ExpressionList<ProjectUser> where;
  @Mock
  private ExpressionList<ProjectUser> expressionList;
  @Mock
  private PagedListFactoryProvider pagedListFactoryProvider;
  @Mock
  private PagedListFactory pagedListFactory;

  @Before
  public void setUp() {
    when(persistence.getRepositoryProvider()).thenReturn(repositoryProvider);
    when(repositoryProvider.getProjectUserRepository()).thenReturn(projectUserRepository);
    when(projectUserRepository.query()).thenReturn(query);
    when(query.where()).thenReturn(where);
    when(where.idEq(any())).thenReturn(expressionList);
    when(where.query()).thenReturn(query);
    when(pagedListFactoryProvider.get()).thenReturn(pagedListFactory);

    target = new ProjectUserRepositoryImpl(
        persistence, validator, activityActor, notificationActor, pagedListFactoryProvider);
  }

  @Test
  public void findByNonExisting() {
    // given
    ProjectUserCriteria criteria = new ProjectUserCriteria();
    PagedList<ProjectUser> pagedList = mock(PagedList.class);

    when(pagedListFactory.createPagedList(any(Query.class))).thenReturn(pagedList);
    when(pagedList.getList()).thenReturn(Collections.emptyList());

    // when
    PagedList<ProjectUser> actual = target.findBy(criteria);

    // then
    Assertions.assertThat(actual).isNotNull();
    Assertions.assertThat(actual.getList()).isEmpty();
  }

  @Test
  public void byIdNonExisting() {
    // given
    long id = 1L;

    // when
    ProjectUser actual = target.byId(id);

    // then
    Assertions.assertThat(actual).isNull();
  }

  @Test
  public void byIdExists() {
    // given
    long id = 1L;
    ProjectUser projectUser = mockProjectUser(id, ProjectRole.Developer);

    mockById(projectUser);

    // when
    ProjectUser actual = target.byId(id);

    // then
    Assertions.assertThat(actual.id).isEqualTo(id);
  }

  @Test
  public void updateRoleOK() {
    // given
    ProjectUser existing = mockProjectUser(1L, ProjectRole.Owner);
    ProjectUser updated = mockProjectUser(1L, ProjectRole.Owner);
    updated.role = ProjectRole.Developer;

    mockById(existing);

    // when
    ProjectUser actual = target.update(updated);

    // then
    Assertions.assertThat(actual).isNotNull();
    Assertions.assertThat(actual.role).isEqualTo(ProjectRole.Developer);
  }

  @Test(expected = ValidationException.class)
  public void updateIdMissing() {
    target.update(new ProjectUser());
  }

  @NotNull
  private ProjectUser mockProjectUser(Long id, ProjectRole role) {
    ProjectUser projectUser = new ProjectUser();
    projectUser.id = id;
    projectUser.user = new User();
    projectUser.user.id = UUID.randomUUID();
    projectUser.project = new Project();
    projectUser.project.id = UUID.randomUUID();
    projectUser.project.owner = projectUser.user;
    projectUser.role = role;
    return projectUser;
  }

  private void mockById(ProjectUser projectUser) {
    ExpressionList<ProjectUser> expressionList = mock(ExpressionList.class);
    when(where.idEq(eq(projectUser.id))).thenReturn(expressionList);
    when(expressionList.findUnique()).thenReturn(projectUser);
  }
}
