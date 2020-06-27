package services.api.impl;

import criterias.AccessTokenCriteria;
import mappers.AccessTokenMapper;
import models.AccessToken;
import models.Scope;
import services.AccessTokenService;
import services.PermissionService;
import services.api.AccessTokenApiService;

import javax.inject.Inject;
import javax.validation.Validator;

public class AccessTokenApiServiceImpl
    extends AbstractApiService<AccessToken, Long, AccessTokenCriteria, AccessTokenService, dto.AccessToken>
    implements AccessTokenApiService {

  @Inject
  public AccessTokenApiServiceImpl(AccessTokenService service, PermissionService permissionService, Validator validator) {
    super(
        service,
        dto.AccessToken.class,
        AccessTokenMapper::toDto,
        new Scope[]{Scope.AccessTokenRead},
        new Scope[]{Scope.AccessTokenWrite},
        permissionService,
        validator);
  }

  @Override
  protected AccessToken toModel(dto.AccessToken dto) {
    return AccessTokenMapper.toModel(dto);
  }
}
