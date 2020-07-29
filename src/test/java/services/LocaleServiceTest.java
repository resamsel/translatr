package services;

import criterias.LocaleCriteria;
import criterias.PagedListFactory;
import models.Locale;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LocaleRepository;
import repositories.Persistence;
import services.impl.CacheServiceImpl;
import services.impl.LocaleServiceImpl;
import utils.CacheApiMock;

import javax.validation.Validator;
import java.util.UUID;

import static assertions.CustomAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static utils.LocaleRepositoryMock.createLocale;

public class LocaleServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceTest.class);

  private LocaleRepository localeRepository;
  private LocaleService target;
  private CacheService cacheService;

  @Test
  public void testById() {
    // mock locale
    Locale locale = createLocale(UUID.randomUUID(), UUID.randomUUID(), "de");
    localeRepository.create(locale);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("locale:id:" + locale.id);
    assertThat(target.byId(locale.id)).nameIsEqualTo("de");
    verify(localeRepository, times(1)).byId(eq(locale.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("locale:id:" + locale.id);
    assertThat(target.byId(locale.id)).nameIsEqualTo("de");
    verify(localeRepository, times(1)).byId(eq(locale.id));

    // This should trigger cache invalidation
    target.update(createLocale(locale, "de-AT"));

    assertThat(cacheService.keys().keySet()).contains("locale:id:" + locale.id);
    assertThat(target.byId(locale.id)).nameIsEqualTo("de-AT");
    verify(localeRepository, times(1)).byId(eq(locale.id));
  }

  @Test
  public void testFindBy() {
    // mock locale
    Locale locale = createLocale(UUID.randomUUID(), UUID.randomUUID(), "de");
    localeRepository.create(locale);

    // This invocation should feed the cache
    LocaleCriteria criteria = new LocaleCriteria().withProjectId(locale.project.id);
    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached")
        .nameIsEqualTo("de");
    verify(localeRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(target.findBy(criteria).getList().get(0))
        .as("cached")
        .nameIsEqualTo("de");
    verify(localeRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    target.update(createLocale(locale, "de-AT"));

    assertThat(target.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("de-AT");
    verify(localeRepository, times(2)).findBy(eq(criteria));
  }

  @Before
  public void before() {
    localeRepository = mock(LocaleRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    target = new LocaleServiceImpl(
            mock(Validator.class),
            cacheService,
            localeRepository,
            mock(LogEntryService.class),
            mock(Persistence.class),
            mock(AuthProvider.class),
            mock(MetricService.class)
    );

    when(localeRepository.create(any())).then(this::persist);
    when(localeRepository.update(any())).then(this::persist);
  }

  private Locale persist(InvocationOnMock a) {
    Locale t = a.getArgument(0);
    when(localeRepository.byId(eq(t.id), any())).thenReturn(t);
    when(localeRepository.findBy(any())).thenReturn(PagedListFactory.create(t));
    return t;
  }
}
