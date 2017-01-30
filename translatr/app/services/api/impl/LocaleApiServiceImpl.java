package services.api.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import controllers.Locales;
import criterias.LocaleCriteria;
import models.Locale;
import models.Scope;
import play.inject.Injector;
import play.mvc.Http.Request;
import services.LocaleService;
import services.api.LocaleApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class LocaleApiServiceImpl extends
    AbstractApiService<Locale, UUID, LocaleCriteria, dto.Locale> implements LocaleApiService {
  private final Injector injector;

  /**
   * @param localeService
   */
  @Inject
  protected LocaleApiServiceImpl(LocaleService localeService, Injector injector) {
    super(localeService, dto.Locale.class, dto.Locale::from, Locale::from,
        new Scope[] {Scope.ProjectRead, Scope.LocaleRead},
        new Scope[] {Scope.ProjectRead, Scope.LocaleWrite});
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public dto.Locale upload(UUID localeId, String fileType, Request request) {
    checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
        Scope.MessageWrite);

    Locale locale = service.byId(localeId);

    injector.instanceOf(Locales.class).importLocale(locale, request);

    return dtoMapper.apply(locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] download(UUID localeId, String fileType) {
    checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
        Scope.MessageRead);

    return injector.instanceOf(Locales.class).download(localeId, fileType);
  }
}
