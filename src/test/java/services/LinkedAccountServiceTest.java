package services;

import actors.ActivityActorRef;
import criterias.GetCriteria;
import criterias.LinkedAccountCriteria;
import criterias.PagedListFactory;
import io.ebean.PagedList;
import models.LinkedAccount;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.LinkedAccountRepository;
import services.impl.LinkedAccountServiceImpl;
import services.impl.NoCacheServiceImpl;
import utils.UserRepositoryMock;

import javax.validation.Validator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static assertions.LinkedAccountAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.LinkedAccountRepositoryMock.createLinkedAccount;

public class LinkedAccountServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LinkedAccountServiceTest.class);

  private LinkedAccountRepository linkedAccountRepository;
  private LinkedAccountService target;
  private CacheService cacheService;
  private User johnSmith;

  @Before
  public void before() {
    linkedAccountRepository = mock(LinkedAccountRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new NoCacheServiceImpl();
    target = new LinkedAccountServiceImpl(
            mock(Validator.class),
            cacheService,
            linkedAccountRepository,
            mock(LogEntryService.class),
            mock(AuthProvider.class),
            mock(MetricService.class),
            mock(ActivityActorRef.class)
    );

    johnSmith = UserRepositoryMock.byUsername("johnsmith");
  }

  @Test
  public void byId() {
    // mock linkedAccount
    LinkedAccount linkedAccount = createLinkedAccount(ThreadLocalRandom.current().nextLong(),
            johnSmith, "google", UUID.randomUUID().toString());
    Http.Request request = mock(Http.Request.class);

    when(linkedAccountRepository.byId(any(GetCriteria.class))).thenReturn(linkedAccount);

    // when
    LinkedAccount actual = target.byId(linkedAccount.id, request);

    // then
    assertThat(actual).providerKeyIsEqualTo(linkedAccount.providerKey);
  }

  @Test
  public void findBy() {
    // given
    LinkedAccount linkedAccount = createLinkedAccount(ThreadLocalRandom.current().nextLong(),
            johnSmith, "google", UUID.randomUUID().toString());
    Http.Request request = mock(Http.Request.class);
    LinkedAccountCriteria criteria = new LinkedAccountCriteria().withUserId(linkedAccount.user.id);

    when(linkedAccountRepository.findBy(eq(criteria))).thenReturn(PagedListFactory.create(linkedAccount));

    // when
    PagedList<LinkedAccount> actual = target.findBy(criteria);

    // then
    assertThat(actual.getList().get(0)).providerKeyIsEqualTo(linkedAccount.providerKey);
  }
}
