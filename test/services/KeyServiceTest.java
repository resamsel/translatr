package services;

import static assertions.KeyAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static utils.KeyRepositoryMock.createKey;

import criterias.HasNextPagedList;
import criterias.KeyCriteria;
import java.util.UUID;
import javax.validation.Validator;
import models.Key;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.KeyRepository;
import services.impl.CacheServiceImpl;
import services.impl.KeyServiceImpl;
import utils.CacheApiMock;

public class KeyServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceTest.class);

  private KeyRepository keyRepository;
  private KeyService keyService;
  private CacheService cacheService;

  @Test
  public void testById() {
    // mock key
    Key key = createKey(UUID.randomUUID(), UUID.randomUUID(), "a");
    keyRepository.create(key);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("key:id:" + key.id);
    assertThat(keyService.byId(key.id)).nameIsEqualTo("a");
    verify(keyRepository, times(1)).byId(eq(key.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("key:id:" + key.id);
    assertThat(keyService.byId(key.id)).nameIsEqualTo("a");
    verify(keyRepository, times(1)).byId(eq(key.id));

    // This should trigger cache invalidation
    keyService.update(createKey(key, "ab"));

    assertThat(cacheService.keys().keySet()).doesNotContain("key:id:" + key.id);
    assertThat(keyService.byId(key.id)).nameIsEqualTo("ab");
    verify(keyRepository, times(2)).byId(eq(key.id));
  }

  @Test
  public void testFindBy() {
    // mock key
    Key key = createKey(UUID.randomUUID(), UUID.randomUUID(), "a");
    keyRepository.create(key);

    // This invocation should feed the cache
    KeyCriteria criteria = new KeyCriteria().withProjectId(key.project.id);
    assertThat(keyService.findBy(criteria).getList().get(0))
        .as("uncached")
        .nameIsEqualTo("a");
    verify(keyRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(keyService.findBy(criteria).getList().get(0))
        .as("cached")
        .nameIsEqualTo("a");
    verify(keyRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    keyService.update(createKey(key, "ab"));

    assertThat(keyService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("ab");
    verify(keyRepository, times(2)).findBy(eq(criteria));
  }

  @Before
  public void before() {
    keyRepository = mock(KeyRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    keyService = new KeyServiceImpl(
        mock(Validator.class),
        cacheService,
        keyRepository,
        mock(LogEntryService.class)
    );

    when(keyRepository.create(any())).then(this::persist);
    when(keyRepository.update(any())).then(this::persist);
  }

  private Key persist(InvocationOnMock a) {
    Key t = a.getArgument(0);
    when(keyRepository.byId(eq(t.id), any())).thenReturn(t);
    when(keyRepository.findBy(any())).thenReturn(HasNextPagedList.create(t));
    return t;
  }
}
