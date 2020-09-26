package services;

import actors.ActivityActorRef;
import criterias.GetCriteria;
import criterias.LocaleCriteria;
import criterias.PagedListFactory;
import io.ebean.PagedList;
import mappers.LocaleMapper;
import models.Locale;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.LocaleRepository;
import repositories.MessageRepository;
import repositories.Persistence;
import services.impl.LocaleServiceImpl;
import services.impl.NoCacheServiceImpl;

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

  @Before
  public void before() {
    localeRepository = mock(LocaleRepository.class,
            withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    target = new LocaleServiceImpl(
            mock(Validator.class),
            new NoCacheServiceImpl(),
            localeRepository,
            mock(LogEntryService.class),
            mock(Persistence.class),
            mock(AuthProvider.class),
            mock(MetricService.class),
            mock(ActivityActorRef.class),
            mock(LocaleMapper.class),
            mock(PermissionService.class),
            mock(MessageRepository.class)
    );
  }

  @Test
  public void byId() {
    // given
    Locale locale = createLocale(UUID.randomUUID(), UUID.randomUUID(), "de");
    Http.Request request = mock(Http.Request.class);

    when(localeRepository.byId(any(GetCriteria.class))).thenReturn(locale);

    // when
    Locale actual = target.byId(locale.id, request);

    // then
    assertThat(actual).nameIsEqualTo("de");
  }

  @Test
  public void findBy() {
    // given
    Locale locale = createLocale(UUID.randomUUID(), UUID.randomUUID(), "de");
    Http.Request request = mock(Http.Request.class);
    LocaleCriteria criteria = LocaleCriteria.from(request).withProjectId(locale.project.id);

    when(localeRepository.findBy(eq(criteria))).thenReturn(PagedListFactory.create(locale));

    // then
    PagedList<Locale> actual = target.findBy(criteria);

    // then
    assertThat(actual.getList().get(0)).nameIsEqualTo("de");
  }
}
