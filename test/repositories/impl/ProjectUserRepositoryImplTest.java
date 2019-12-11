package repositories.impl;

import akka.actor.ActorRef;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import criterias.PagedListFactory;
import criterias.ProjectUserCriteria;
import models.ProjectUser;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import repositories.PagedListFactoryProvider;
import repositories.RepositoryProvider;

import javax.validation.Validator;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectUserRepositoryImplTest {

  private ProjectUserRepositoryImpl target;
  @Mock
  private Validator validator;
  @Mock
  private ActorRef activityActor;
  @Mock
  private ActorRef notificationActor;
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
  private PagedListFactory pageListFactory;

  @Before
  public void setUp() {
    when(repositoryProvider.getProjectUserRepository()).thenReturn(projectUserRepository);
    when(projectUserRepository.query()).thenReturn(query);
    when(query.where()).thenReturn(where);
    when(where.idEq(any())).thenReturn(expressionList);
    when(pagedListFactoryProvider.get()).thenReturn(pageListFactory);

    target = new ProjectUserRepositoryImpl(
        validator, activityActor, notificationActor, repositoryProvider, pagedListFactoryProvider);
  }

  @Test
  public void findByNonExisting() {
    // given
    ProjectUserCriteria criteria = new ProjectUserCriteria();
    PagedList<ProjectUser> pagedList = mock(PagedList.class);

    when(pageListFactory.createPagedList(any(Query.class))).thenReturn(pagedList);
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
    ProjectUser projectUser = new ProjectUser();
    projectUser.id = id;

    mockById(id, projectUser);

    // when
    ProjectUser actual = target.byId(id);

    // then
    Assertions.assertThat(actual.id).isEqualTo(id);
  }

  private void mockById(long id, ProjectUser projectUser) {
    ExpressionList<ProjectUser> expressionList = mock(ExpressionList.class);
    when(where.idEq(eq(id))).thenReturn(expressionList);
    when(expressionList.findUnique()).thenReturn(projectUser);
  }
}
