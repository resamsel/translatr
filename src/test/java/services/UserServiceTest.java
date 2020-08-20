package services;

import criterias.PagedListFactory;
import criterias.UserCriteria;
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

import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static assertions.UserAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static utils.UserRepositoryMock.createUser;

public class UserServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceTest.class);

  private UserRepository userRepository;
  private UserService target;
  private CacheService cacheService;
  private LinkedAccountService linkedAccountService;
  private AccessTokenService accessTokenService;
  private LogEntryService logEntryService;
  private ProjectService projectService;
  private ProjectUserService projecUserService;

  @Test
  public void testById() {
    // mock user
    User user = createUser(UUID.randomUUID(), "a", "b", "a@b.com");
    userRepository.create(user);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("user:id:" + user.id);
    assertThat(target.byId(user.id)).nameIsEqualTo("a");
    verify(userRepository, times(1)).byId(eq(user.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("user:id:" + user.id);
    assertThat(target.byId(user.id)).nameIsEqualTo("a");
    verify(userRepository, times(1)).byId(eq(user.id));

    // This should trigger cache invalidation
    target.update(createUser(user, "ab", "b", "a@b.com"));

    assertThat(cacheService.keys().keySet()).contains("user:id:" + user.id);
    assertThat(target.byId(user.id)).nameIsEqualTo("ab");
    verify(userRepository, times(1)).byId(eq(user.id));
  }

  @Test
  public void testFindBy() {
    // mock user
    User user = createUser(UUID.randomUUID(), "a", "b", "a@b.com");
    userRepository.create(user);

    // This invocation should feed the cache
    UserCriteria criteria = new UserCriteria().withSearch(user.name);
    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached")
        .nameIsEqualTo("a");
    verify(userRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(target.findBy(criteria).getList().get(0))
        .as("cached")
        .nameIsEqualTo("a");
    verify(userRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    target.update(createUser(user, "ab", "b", "a@b.com"));

    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("ab");
    verify(userRepository, times(2)).findBy(eq(criteria));
  }

  @Test
  public void testMerge() {
    User user = createUser(UUID.randomUUID(), "a", "a", "a@a.com");
    User otherUser = createUser(UUID.randomUUID(), "b", "b", "b@b.com");
    userRepository.create(user);
    userRepository.create(otherUser);

    target.merge(user, otherUser);

    assertThat(userRepository.byId(user.id)).nameIsEqualTo("a").activeIsTrue();
    assertThat(userRepository.byId(otherUser.id)).nameIsEqualTo("b").activeIsFalse();

    verify(linkedAccountService, times(1)).save(anyList());
    verify(accessTokenService, times(1)).save(anyList());
    verify(logEntryService, times(1)).save(anyList());
    verify(projectService, times(1)).save(anyList());
    verify(projecUserService, times(1)).save(anyList());
  }

  @Before
  public void before() {
    userRepository = mock(UserRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    linkedAccountService = mock(LinkedAccountService.class);
    accessTokenService = mock(AccessTokenService.class);
    logEntryService = mock(LogEntryService.class);
    projectService = mock(ProjectService.class);
    projecUserService = mock(ProjectUserService.class);
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
            mock(MetricService.class)
    );

    when(userRepository.create(any())).then(this::persist);
    when(userRepository.update(any())).then(this::persist);
    when(userRepository.save((User) any())).then(this::persist);
    when(userRepository.save(anyList())).then(this::persistList);
    when(linkedAccountService.findBy(any())).thenReturn(PagedListFactory.create());
    when(accessTokenService.findBy(any())).thenReturn(PagedListFactory.create());
    when(projectService.findBy(any())).thenReturn(PagedListFactory.create());
    when(projecUserService.findBy(any())).thenReturn(PagedListFactory.create());
    when(logEntryService.findBy(any())).thenReturn(PagedListFactory.create());
  }

  private User updateMocks(User t) {
    LOGGER.debug("updateMocks({})", t);

    if (t.id == null) {
      t.id = UUID.randomUUID();
    }
    t.linkedAccounts = new ArrayList<>(t.linkedAccounts);

    when(userRepository.byId(eq(t.id), any())).thenReturn(t);
    when(userRepository.findBy(any())).thenReturn(PagedListFactory.create(t));

    return t;
  }

  private User persist(InvocationOnMock a) {
    return updateMocks(a.getArgument(0));
  }

  private List<User> persistList(InvocationOnMock a) {
    List<User> t = a.getArgument(0);
    t.forEach(this::updateMocks);
    when(userRepository.findBy(any())).thenReturn(PagedListFactory.create(t));
    return t;
  }

}
