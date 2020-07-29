package services;

import criterias.LinkedAccountCriteria;
import criterias.PagedListFactory;
import models.LinkedAccount;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LinkedAccountRepository;
import services.impl.CacheServiceImpl;
import services.impl.LinkedAccountServiceImpl;
import utils.CacheApiMock;
import utils.UserRepositoryMock;

import javax.validation.Validator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static assertions.LinkedAccountAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
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

  @Test
  public void testById() {
    // mock linkedAccount
    LinkedAccount linkedAccount = createLinkedAccount(ThreadLocalRandom.current().nextLong(),
        johnSmith, "google", UUID.randomUUID().toString());
    linkedAccountRepository.create(linkedAccount);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("linkedAccount:id:" + linkedAccount.id);
    assertThat(target.byId(linkedAccount.id))
        .providerKeyIsEqualTo(linkedAccount.providerKey);
    verify(linkedAccountRepository, times(1)).byId(eq(linkedAccount.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("linkedAccount:id:" + linkedAccount.id);
    assertThat(target.byId(linkedAccount.id))
        .providerKeyIsEqualTo(linkedAccount.providerKey);
    verify(linkedAccountRepository, times(1)).byId(eq(linkedAccount.id));

    // This should trigger cache invalidation
    target
        .update(createLinkedAccount(linkedAccount, "facebook", UUID.randomUUID().toString()));

    assertThat(cacheService.keys().keySet()).contains("linkedAccount:id:" + linkedAccount.id);
    assertThat(target.byId(linkedAccount.id)).providerKeyIsEqualTo("facebook");
    verify(linkedAccountRepository, times(1)).byId(eq(linkedAccount.id));
  }

  @Test
  public void testFindBy() {
    // mock linkedAccount
    LinkedAccount linkedAccount = createLinkedAccount(ThreadLocalRandom.current().nextLong(),
        johnSmith, "google", UUID.randomUUID().toString());
    linkedAccountRepository.create(linkedAccount);

    // This invocation should feed the cache
    LinkedAccountCriteria criteria = new LinkedAccountCriteria().withUserId(linkedAccount.user.id);
    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached")
        .providerKeyIsEqualTo(linkedAccount.providerKey);
    verify(linkedAccountRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(target.findBy(criteria).getList().get(0))
        .as("cached")
        .providerKeyIsEqualTo(linkedAccount.providerKey);
    verify(linkedAccountRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    target
        .update(createLinkedAccount(linkedAccount, "facebook", UUID.randomUUID().toString()));

    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .providerKeyIsEqualTo("facebook");
    verify(linkedAccountRepository, times(2)).findBy(eq(criteria));
  }

  @Before
  public void before() {
    linkedAccountRepository = mock(LinkedAccountRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    target = new LinkedAccountServiceImpl(
            mock(Validator.class),
            cacheService,
            linkedAccountRepository,
            mock(LogEntryService.class),
            mock(AuthProvider.class),
            mock(MetricService.class)
    );

    johnSmith = UserRepositoryMock.byUsername("johnsmith");

    when(linkedAccountRepository.create(any())).then(this::persist);
    when(linkedAccountRepository.update(any())).then(this::persist);
  }

  private LinkedAccount persist(InvocationOnMock a) {
    LinkedAccount t = a.getArgument(0);
    when(linkedAccountRepository.byId(eq(t.id), any())).thenReturn(t);
    when(linkedAccountRepository.findBy(any())).thenReturn(PagedListFactory.create(t));
    return t;
  }
}
