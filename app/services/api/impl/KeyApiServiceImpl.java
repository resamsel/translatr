package services.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import criterias.KeyCriteria;

import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

import dto.NotFoundException;
import models.Key;
import models.Locale;
import models.Scope;
import play.libs.Json;
import services.KeyService;
import services.PermissionService;
import services.ProjectService;
import services.api.KeyApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class KeyApiServiceImpl extends
    AbstractApiService<Key, UUID, KeyCriteria, KeyService, dto.Key>
    implements KeyApiService {

  private final ProjectService projectService;

  @Inject
  protected KeyApiServiceImpl(KeyService localeService, ProjectService projectService,
                              PermissionService permissionService) {
    super(localeService, dto.Key.class, dto.Key::from,
        new Scope[]{Scope.ProjectRead, Scope.KeyRead},
        new Scope[]{Scope.ProjectRead, Scope.KeyWrite},
        permissionService);

    this.projectService = projectService;
  }

  @Override
  public dto.Key byOwnerAndProjectAndName(String username, String projectName, String keyName, String... fetches) {
    permissionService
        .checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.KeyRead,
            Scope.MessageRead);

    Key key = service.byOwnerAndProjectAndName(username, projectName, keyName, fetches);
    if (key == null) {
      throw new NotFoundException(dto.Key.class.getSimpleName(), keyName);
    }

    return dtoMapper.apply(key);
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
