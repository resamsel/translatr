package services.impl;

import actors.ActivityActorRef;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import play.mvc.Http;
import repositories.ProjectUserRepository;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;
import services.MetricService;
import validators.ProjectUserModifyAllowedValidator;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.CustomAssertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectUserServiceImplTest {

  @Mock
  private Validator validator;
  @Mock
  private CacheService cache;
  @Mock
  private ProjectUserRepository projectUserRepository;
  @Mock
  private LogEntryService logEntryService;
  @Mock
  private AuthProvider authProvider;
  @Mock
  private MetricService metricService;
  @Mock
  private ActivityActorRef activityActor;
  @Mock
  private ProjectUserModifyAllowedValidator projectUserModifyAllowedValidator;

  private ProjectUserServiceImpl target;

  @Before
  public void setUp() {
    target = new ProjectUserServiceImpl(
            validator, cache, projectUserRepository, logEntryService, authProvider, metricService, activityActor,
            projectUserModifyAllowedValidator);
  }

  @Test
  public void createAsAdmin() {
    // given
    ProjectUser model = new ProjectUser()
            .withUser(new User().withId(UUID.randomUUID()))
            .withProject(new Project().withId(UUID.randomUUID()))
            .withRole(ProjectRole.Owner);
    Long id = 1L;
    Http.Request request = Mockito.mock(Http.Request.class);

    when(projectUserModifyAllowedValidator.isValid(eq(model), eq(request))).thenReturn(true);

    when(projectUserRepository.create(model))
            .thenReturn(new ProjectUser()
                    .withId(id)
                    .withUser(model.user)
                    .withProject(model.project)
                    .withRole(model.role));

    // when
    ProjectUser actual = target.create(model, request);

    // then
    assertThat(actual)
            .userIdIsEqualTo(model.user.id)
            .projectIdIsEqualTo(model.project.id)
            .roleIsEqualTo(ProjectRole.Owner);
  }
}
