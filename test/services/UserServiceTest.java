package services;

import static assertions.UserAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static utils.UserRepositoryMock.createUser;

import criterias.HasNextPagedList;
import criterias.UserCriteria;
import java.util.UUID;
import javax.validation.Validator;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.UserRepository;
import services.impl.CacheServiceImpl;
import services.impl.UserServiceImpl;
import utils.CacheApiMock;

public class UserServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceTest.class);

  private UserRepository userRepository;
  private UserService userService;
  private CacheService cacheService;

  @Test
  public void testById() {
    // mock user
    User user = createUser(UUID.randomUUID(), "a", "b", "a@b.com");
    userRepository.create(user);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("user:id:" + user.id);
    assertThat(userService.byId(user.id)).nameIsEqualTo("a");
    verify(userRepository, times(1)).byId(eq(user.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("user:id:" + user.id);
    assertThat(userService.byId(user.id)).nameIsEqualTo("a");
    verify(userRepository, times(1)).byId(eq(user.id));

    // This should trigger cache invalidation
    userService.update(createUser(user, "ab", "b", "a@b.com"));

    assertThat(cacheService.keys().keySet()).doesNotContain("user:id:" + user.id);
    assertThat(userService.byId(user.id)).nameIsEqualTo("ab");
    verify(userRepository, times(2)).byId(eq(user.id));
  }

  @Test
  public void testFindBy() {
    // mock user
    User user = createUser(UUID.randomUUID(), "a", "b", "a@b.com");
    userRepository.create(user);

    // This invocation should feed the cache
    UserCriteria criteria = new UserCriteria().withSearch(user.name);
    assertThat(userService.findBy(criteria).getList().get(0))
        .as("uncached")
        .nameIsEqualTo("a");
    verify(userRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(userService.findBy(criteria).getList().get(0))
        .as("cached")
        .nameIsEqualTo("a");
    verify(userRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    userService.update(createUser(user, "ab", "b", "a@b.com"));

    assertThat(userService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("ab");
    verify(userRepository, times(2)).findBy(eq(criteria));
  }

  @Before
  public void before() {
    userRepository = mock(UserRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    userService = new UserServiceImpl(
        mock(Validator.class),
        cacheService,
        userRepository,
        mock(LinkedAccountService.class),
        mock(AccessTokenService.class),
        mock(ProjectService.class),
        mock(ProjectUserService.class),
        mock(LogEntryService.class)
    );

    when(userRepository.create(any())).then(this::persist);
    when(userRepository.update(any())).then(this::persist);
  }

  private User persist(InvocationOnMock a) {
    User t = a.getArgument(0);
    when(userRepository.byId(eq(t.id), any())).thenReturn(t);
    when(userRepository.findBy(any())).thenReturn(HasNextPagedList.create(t));
    return t;
  }
}
