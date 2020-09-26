package services;

import actors.ActivityActorRef;
import criterias.AccessTokenCriteria;
import criterias.GetCriteria;
import criterias.PagedListFactory;
import io.ebean.PagedList;
import models.AccessToken;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.AccessTokenRepository;
import services.impl.AccessTokenServiceImpl;
import services.impl.CacheServiceImpl;
import services.impl.NoCacheServiceImpl;
import utils.UserRepositoryMock;

import javax.validation.Validator;
import java.util.concurrent.ThreadLocalRandom;

import static assertions.AccessTokenAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.AccessTokenRepositoryMock.createAccessToken;

public class AccessTokenServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenServiceTest.class);

  private AccessTokenRepository accessTokenRepository;
  private AccessTokenService target;
  private CacheService cacheService;
  private User johnSmith;

  @Before
  public void before() {
    accessTokenRepository = mock(AccessTokenRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new NoCacheServiceImpl();
    target = new AccessTokenServiceImpl(
            mock(Validator.class),
            cacheService,
            mock(AuthProvider.class),
            accessTokenRepository,
            mock(LogEntryService.class),
            mock(MetricService.class),
            mock(ActivityActorRef.class)
    );

    johnSmith = UserRepositoryMock.byUsername("johnsmith");
  }

  @Test
  public void byId() {
    // given
    AccessToken accessToken = createAccessToken(ThreadLocalRandom.current().nextLong(), johnSmith,
            "de");
    Http.Request request = mock(Http.Request.class);

    when(accessTokenRepository.byId(any(GetCriteria.class))).thenReturn(accessToken);

    // when
    AccessToken actual = target.byId(accessToken.id, request);

    // then
    assertThat(actual).nameIsEqualTo("de");
  }

  @Test
  public void findBy() {
    // given
    AccessToken accessToken = createAccessToken(ThreadLocalRandom.current().nextLong(), johnSmith,
            "de");
    Http.Request request = mock(Http.Request.class);
    AccessTokenCriteria criteria = new AccessTokenCriteria().withUserId(accessToken.user.id);

    when(accessTokenRepository.findBy(eq(criteria))).thenReturn(PagedListFactory.create(accessToken));

    // when
    PagedList<AccessToken> actual = target.findBy(criteria);

    // then
    assertThat(actual.getList().get(0)).nameIsEqualTo("de");
  }
}
