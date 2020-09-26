package services;

import actors.ActivityActorRef;
import criterias.GetCriteria;
import criterias.PagedListFactory;
import criterias.UserCriteria;
import io.ebean.PagedList;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.UserRepository;
import services.impl.NoCacheServiceImpl;
import services.impl.UserServiceImpl;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.UserAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.UserRepositoryMock.createUser;

public class UserServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceTest.class);

  private UserRepository userRepository;
  private UserService target;

  @Before
  public void before() {
    userRepository = mock(UserRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    CacheService cacheService = new NoCacheServiceImpl();
    LinkedAccountService linkedAccountService = mock(LinkedAccountService.class);
    AccessTokenService accessTokenService = mock(AccessTokenService.class);
    LogEntryService logEntryService = mock(LogEntryService.class);
    ProjectService projectService = mock(ProjectService.class);
    ProjectUserService projecUserService = mock(ProjectUserService.class);
    target = new UserServiceImpl(
            mock(Validator.class),
            cacheService,
            userRepository,
            linkedAccountService,
            accessTokenService,
            projectService,
            projecUserService,
            logEntryService,
            mock(AuthProvider.class),
            mock(MetricService.class),
            mock(ActivityActorRef.class)
    );

    when(linkedAccountService.findBy(any())).thenReturn(PagedListFactory.create());
    when(accessTokenService.findBy(any())).thenReturn(PagedListFactory.create());
    when(projectService.findBy(any())).thenReturn(PagedListFactory.create());
    when(projecUserService.findBy(any())).thenReturn(PagedListFactory.create());
    when(logEntryService.findBy(any())).thenReturn(PagedListFactory.create());
  }

  @Test
  public void byId() {
    // given
    User user = createUser(UUID.randomUUID(), "a", "b", "a@b.com");
    Http.Request request = mock(Http.Request.class);

    when(userRepository.byId(any(GetCriteria.class))).thenReturn(user);

    // when
    User actual = target.byId(user.id, request);

    // then
    assertThat(actual).nameIsEqualTo("a");
  }

  @Test
  public void findBy() {
    // given
    User user = createUser(UUID.randomUUID(), "a", "b", "a@b.com");
    userRepository.create(user);
    Http.Request request = mock(Http.Request.class);
    UserCriteria criteria = UserCriteria.from(request).withSearch(user.name);

    when(userRepository.findBy(eq(criteria))).thenReturn(PagedListFactory.create(user));

    // when
    PagedList<User> actual = target.findBy(criteria);

    // then
    assertThat(actual.getList().get(0)).nameIsEqualTo("a");
  }

  @Test
  public void testMerge() {
    // given
    Http.Request request = mock(Http.Request.class);
    User user = createUser(UUID.randomUUID(), "a", "a", "a@a.com");
    User otherUser = createUser(UUID.randomUUID(), "b", "b", "b@b.com");

    // when
    User actual = target.merge(user, otherUser, request);

    // then
    assertThat(actual).nameIsEqualTo("a").activeIsTrue();
  }
}
