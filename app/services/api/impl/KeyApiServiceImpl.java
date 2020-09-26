package services.api.impl;

import criterias.KeyCriteria;
import dto.NotFoundException;
import mappers.KeyMapper;
import models.Key;
import models.Scope;
import play.mvc.Http;
import services.KeyService;
import services.PermissionService;
import services.ProjectService;
import services.api.KeyApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.UUID;

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
                              PermissionService permissionService, Validator validator, KeyMapper keyMapper) {
    super(localeService, dto.Key.class, keyMapper::toDto,
        new Scope[]{Scope.ProjectRead, Scope.KeyRead},
        new Scope[]{Scope.ProjectRead, Scope.KeyWrite},
        permissionService,
        validator);

    this.projectService = projectService;
  }

  @Override
  public dto.Key byOwnerAndProjectAndName(Http.Request request, String username, String projectName, String keyName, String... fetches) {
    permissionService
        .checkPermissionAll(request, "Access token not allowed", Scope.ProjectRead, Scope.KeyRead,
            Scope.MessageRead);

    Key key = service.byOwnerAndProjectAndName(username, projectName, keyName, request, fetches);
    if (key == null) {
      throw new NotFoundException(dto.Key.class.getSimpleName(), keyName);
    }

    return dtoMapper.apply(key, request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Key toModel(dto.Key in) {
    return KeyMapper.toModel(in, projectService.byId(in.projectId, null /* FIXME */));
  }
}
