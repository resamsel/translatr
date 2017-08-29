package services;

import static assertions.LocaleAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static utils.LocaleRepositoryMock.createLocale;

import criterias.HasNextPagedList;
import criterias.LocaleCriteria;
import java.util.UUID;
import javax.validation.Validator;
import models.Locale;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LocaleRepository;
import services.impl.CacheServiceImpl;
import services.impl.LocaleServiceImpl;
import utils.CacheApiMock;

public class LocaleServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceTest.class);

  private LocaleRepository localeRepository;
  private LocaleService localeService;
  private CacheService cacheService;

  @Test
  public void testById() {
    // mock locale
    Locale locale = createLocale(UUID.randomUUID(), UUID.randomUUID(), "de");
    localeRepository.create(locale);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("locale:id:" + locale.id);
    assertThat(localeService.byId(locale.id)).nameIsEqualTo("de");
    verify(localeRepository, times(1)).byId(eq(locale.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("locale:id:" + locale.id);
    assertThat(localeService.byId(locale.id)).nameIsEqualTo("de");
    verify(localeRepository, times(1)).byId(eq(locale.id));

    // This should trigger cache invalidation
    localeService.update(createLocale(locale, "de-AT"));

    assertThat(cacheService.keys().keySet()).contains("locale:id:" + locale.id);
    assertThat(localeService.byId(locale.id)).nameIsEqualTo("de-AT");
    verify(localeRepository, times(1)).byId(eq(locale.id));
  }

  @Test
  public void testFindBy() {
    // mock locale
    Locale locale = createLocale(UUID.randomUUID(), UUID.randomUUID(), "de");
    localeRepository.create(locale);

    // This invocation should feed the cache
    LocaleCriteria criteria = new LocaleCriteria().withProjectId(locale.project.id);
    assertThat(localeService.findBy(criteria).getList().get(0))
        .as("uncached")
        .nameIsEqualTo("de");
    verify(localeRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(localeService.findBy(criteria).getList().get(0))
        .as("cached")
        .nameIsEqualTo("de");
    verify(localeRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    localeService.update(createLocale(locale, "de-AT"));

    assertThat(localeService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .nameIsEqualTo("de-AT");
    verify(localeRepository, times(2)).findBy(eq(criteria));
  }

  @Before
  public void before() {
    localeRepository = mock(LocaleRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    localeService = new LocaleServiceImpl(
        mock(Validator.class),
        cacheService,
        localeRepository,
        mock(LogEntryService.class)
    );

    when(localeRepository.create(any())).then(this::persist);
    when(localeRepository.update(any())).then(this::persist);
  }

  private Locale persist(InvocationOnMock a) {
    Locale t = a.getArgument(0);
    when(localeRepository.byId(eq(t.id), any())).thenReturn(t);
    when(localeRepository.findBy(any())).thenReturn(HasNextPagedList.create(t));
    return t;
  }
}
