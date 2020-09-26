package services.api.impl;

import criterias.AccessTokenCriteria;
import mappers.AccessTokenMapper;
import models.AccessToken;
import models.Scope;
import play.mvc.Http;
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
        (in, request) -> AccessTokenMapper.toDto(in),
        new Scope[]{Scope.AccessTokenRead},
        new Scope[]{Scope.AccessTokenWrite},
        permissionService,
        validator);
  }

  @Override
  protected AccessToken toModel(dto.AccessToken in, Http.Request request) {
    return AccessTokenMapper.toModel(in);
  }
}
