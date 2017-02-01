package services.api.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

import criterias.KeyCriteria;
import models.Key;
import models.Scope;
import play.libs.Json;
import services.KeyService;
import services.ProjectService;
import services.api.KeyApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class KeyApiServiceImpl extends AbstractApiService<Key, UUID, KeyCriteria, dto.Key>
    implements KeyApiService {
  private final ProjectService projectService;

  /**
   * @param localeService
   */
  @Inject
  protected KeyApiServiceImpl(KeyService localeService, ProjectService projectService) {
    super(localeService, dto.Key.class, dto.Key::from,
        new Scope[] {Scope.ProjectRead, Scope.KeyRead},
        new Scope[] {Scope.ProjectRead, Scope.KeyWrite});
    this.projectService = projectService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Key toModel(JsonNode json) {
    dto.Key dto = Json.fromJson(json, dto.Key.class);

    return dto.toModel(projectService.byId(dto.projectId));
  }
}
