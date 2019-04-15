package services.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import criterias.AccessTokenCriteria;
import models.AccessToken;
import models.Scope;
import play.libs.Json;
import services.AccessTokenService;
import services.PermissionService;
import services.api.AccessTokenApiService;

import javax.inject.Inject;

public class AccessTokenApiServiceImpl
    extends AbstractApiService<AccessToken, Long, AccessTokenCriteria, AccessTokenService, dto.AccessToken>
    implements AccessTokenApiService {

  @Inject
  public AccessTokenApiServiceImpl(AccessTokenService service, PermissionService permissionService) {
    super(
        service,
        dto.AccessToken.class,
        dto.AccessToken::from,
        new Scope[]{Scope.AccessTokenRead},
        new Scope[]{Scope.AccessTokenWrite},
        permissionService);
  }

  @Override
  protected AccessToken toModel(JsonNode json) {
    return Json.fromJson(json, dto.AccessToken.class).toModel();
  }
}
