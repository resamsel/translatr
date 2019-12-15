package services.impl;

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
import repositories.ProjectUserRepository;
import services.CacheService;
import services.LogEntryService;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.CustomAssertions.assertThat;

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

  private ProjectUserServiceImpl target;

  @Before
  public void setUp() {
    target = new ProjectUserServiceImpl(validator, cache, projectUserRepository, logEntryService);
  }

  @Test
  public void createAsAdmin() {
    // given
    ProjectUser model = new ProjectUser()
        .withUser(new User().withId(UUID.randomUUID()))
        .withProject(new Project().withId(UUID.randomUUID()))
        .withRole(ProjectRole.Owner);
    Long id = 1L;

    Mockito.when(projectUserRepository.save(model))
        .thenReturn(new ProjectUser()
            .withId(id)
            .withUser(model.user)
            .withProject(model.project)
            .withRole(model.role));

    // when
    ProjectUser actual = target.create(model);

    // then
    assertThat(actual)
        .userIdIsEqualTo(model.user.id)
        .projectIdIsEqualTo(model.project.id)
        .roleIsEqualTo(ProjectRole.Owner);
  }
}
