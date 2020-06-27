package services;

import criterias.KeyCriteria;
import criterias.PagedListFactory;
import models.Key;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import repositories.KeyRepository;
import repositories.Persistence;
import services.impl.CacheServiceImpl;
import services.impl.KeyServiceImpl;
import utils.CacheApiMock;
import utils.ProjectRepositoryMock;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.KeyAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.KeyRepositoryMock.createKey;

public class KeyServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceTest.class);

  private KeyRepository keyRepository;
  private KeyService target;
  private CacheService cacheService;

  @Test
  public void testById() {
    // mock key
    Key key = createKey(UUID.randomUUID(), UUID.randomUUID(), "a");
    keyRepository.create(key);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("key:id:" + key.id);
    assertThat(target.byId(key.id)).nameIsEqualTo("a");
    verify(keyRepository, times(1)).byId(eq(key.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("key:id:" + key.id);
    assertThat(target.byId(key.id)).nameIsEqualTo("a");
    verify(keyRepository, times(1)).byId(eq(key.id));

    // This should trigger cache invalidation
    target.update(createKey(key, "ab"));

    assertThat(cacheService.keys().keySet()).contains("key:id:" + key.id);
    assertThat(target.byId(key.id)).nameIsEqualTo("ab");
    verify(keyRepository, times(1)).byId(eq(key.id));
  }

  @Test
  public void testFindBy() {
    // mock key
    Key key = createKey(UUID.randomUUID(), UUID.randomUUID(), "a");
    keyRepository.create(key);

    // This invocation should feed the cache
    KeyCriteria criteria = new KeyCriteria().withProjectId(key.project.id);
    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached")
        .nameIsEqualTo("a");
    verify(keyRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(target.findBy(criteria).getList().get(0))
        .as("cached")
        .nameIsEqualTo("a");
    verify(keyRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    target.update(createKey(key, "ab"));

    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("ab");
    verify(keyRepository, times(2)).findBy(eq(criteria));

    LOGGER.debug("Cache keys before key creation: {}", cacheService.keys().keySet());
    LOGGER.debug("Project ID: {}", key.project.id);
    // This should trigger cache invalidation
    target.create(createKey(UUID.randomUUID(), key.project.id, "c"));
    LOGGER.debug("Cache keys after key creation: {}", cacheService.keys().keySet());

    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached (invalidated after creation)")
        .nameIsEqualTo("c");
    verify(keyRepository, times(3)).findBy(eq(criteria));
  }

  @Test
  public void testByProjectAndName() {
    // mock key
    Key key = createKey(UUID.randomUUID(),
        ProjectRepositoryMock.byOwnerAndName("johnsmith", "project1"), "a");
    keyRepository.create(key);

    // This invocation should feed the cache
    assertThat(target.byProjectAndName(key.project, key.name)).nameIsEqualTo("a");
    verify(keyRepository, times(1)).byProjectAndName(eq(key.project), eq(key.name));

    // This invocation should use the cache, not the repository
    assertThat(target.byProjectAndName(key.project, key.name)).nameIsEqualTo("a");
    verify(keyRepository, times(1)).byProjectAndName(eq(key.project), eq(key.name));

    // This should trigger cache invalidation
    key = createKey(key, "ab");
    target.update(key);

    assertThat(cacheService.keys().keySet())
            .doesNotContain("key:project:" + key.project.id + ":name:a");
    assertThat(target.byProjectAndName(key.project, key.name)).nameIsEqualTo("ab");
    verify(keyRepository, times(1)).byProjectAndName(eq(key.project), eq(key.name));
  }

  @Before
  public void before() {
    keyRepository = mock(KeyRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    target = new KeyServiceImpl(
            mock(Validator.class),
            cacheService,
            keyRepository,
            mock(LogEntryService.class),
            mock(Persistence.class),
            mock(AuthProvider.class),
            mock(MetricService.class)
    );

    when(keyRepository.save((Key) any())).then(this::persist);
    when(keyRepository.create(any())).then(this::persist);
    when(keyRepository.update(any())).then(this::persist);
  }

  private Key persist(InvocationOnMock a) {
    Key t = a.getArgument(0);
    if(t.id == null)
      t.id = UUID.randomUUID();
    LOGGER.debug("mock: persisting {} - {}", t.getClass(), Json.toJson(t));
    when(keyRepository.byId(eq(t.id), any())).thenReturn(t);
    when(keyRepository.byProjectAndName(eq(t.project), eq(t.name))).thenReturn(t);
    when(keyRepository.findBy(any())).thenReturn(PagedListFactory.create(t));
    return t;
  }
}
