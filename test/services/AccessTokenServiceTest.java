package services;

import static assertions.AccessTokenAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static utils.AccessTokenRepositoryMock.createAccessToken;

import criterias.AccessTokenCriteria;
import criterias.HasNextPagedList;
import java.util.concurrent.ThreadLocalRandom;
import javax.validation.Validator;
import models.AccessToken;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.AccessTokenRepository;
import services.impl.AccessTokenServiceImpl;
import services.impl.CacheServiceImpl;
import utils.CacheApiMock;
import utils.UserRepositoryMock;

public class AccessTokenServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenServiceTest.class);

  private AccessTokenRepository accessTokenRepository;
  private AccessTokenService accessTokenService;
  private CacheService cacheService;
  private User johnSmith;

  @Test
  public void testById() {
    // mock accessToken
    AccessToken accessToken = createAccessToken(ThreadLocalRandom.current().nextLong(), johnSmith,
        "de");
    accessTokenRepository.create(accessToken);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("accessToken:id:" + accessToken.id);
    assertThat(accessTokenService.byId(accessToken.id)).nameIsEqualTo("de");
    verify(accessTokenRepository, times(1)).byId(eq(accessToken.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("accessToken:id:" + accessToken.id);
    assertThat(accessTokenService.byId(accessToken.id)).nameIsEqualTo("de");
    verify(accessTokenRepository, times(1)).byId(eq(accessToken.id));

    // This should trigger cache invalidation
    accessTokenService.update(createAccessToken(accessToken, "de-AT"));

    assertThat(cacheService.keys().keySet()).doesNotContain("accessToken:id:" + accessToken.id);
    assertThat(accessTokenService.byId(accessToken.id)).nameIsEqualTo("de-AT");
    verify(accessTokenRepository, times(2)).byId(eq(accessToken.id));
  }

  @Test
  public void testFindBy() {
    // mock accessToken
    AccessToken accessToken = createAccessToken(ThreadLocalRandom.current().nextLong(), johnSmith,
        "de");
    accessTokenRepository.create(accessToken);

    // This invocation should feed the cache
    AccessTokenCriteria criteria = new AccessTokenCriteria().withUserId(accessToken.user.id);
    assertThat(accessTokenService.findBy(criteria).getList().get(0))
        .as("uncached")
        .nameIsEqualTo("de");
    verify(accessTokenRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(accessTokenService.findBy(criteria).getList().get(0))
        .as("cached")
        .nameIsEqualTo("de");
    verify(accessTokenRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    accessTokenService.update(createAccessToken(accessToken, "de-AT"));

    assertThat(accessTokenService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("de-AT");
    verify(accessTokenRepository, times(2)).findBy(eq(criteria));
  }

  @Before
  public void before() {
    accessTokenRepository = mock(AccessTokenRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    accessTokenService = new AccessTokenServiceImpl(
        mock(Validator.class),
        cacheService,
        accessTokenRepository,
        mock(LogEntryService.class)
    );

    johnSmith = UserRepositoryMock.byUsername("johnsmith");

    when(accessTokenRepository.create(any())).then(this::persist);
    when(accessTokenRepository.update(any())).then(this::persist);
  }

  private AccessToken persist(InvocationOnMock a) {
    AccessToken t = a.getArgument(0);
    when(accessTokenRepository.byId(eq(t.id), any())).thenReturn(t);
    when(accessTokenRepository.findBy(any())).thenReturn(HasNextPagedList.create(t));
    return t;
  }
}
