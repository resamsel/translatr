package services.api.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import criterias.KeyCriteria;
import models.Key;
import models.Scope;
import services.KeyService;
import services.api.KeyApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class KeyApiServiceImpl extends AbstractApiService<Key, UUID, KeyCriteria, dto.Key>
    implements KeyApiService {
  /**
   * @param localeService
   */
  @Inject
  protected KeyApiServiceImpl(KeyService localeService) {
    super(localeService, dto.Key.class, dto.Key::from, Key::from,
        new Scope[] {Scope.ProjectRead, Scope.KeyRead},
        new Scope[] {Scope.ProjectRead, Scope.KeyWrite});
  }
}
