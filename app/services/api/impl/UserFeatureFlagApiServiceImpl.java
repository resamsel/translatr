package services.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import criterias.UserFeatureFlagCriteria;
import mappers.UserFeatureFlagMapper;
import models.Scope;
import models.UserFeatureFlag;
import play.libs.Json;
import services.PermissionService;
import services.UserFeatureFlagService;
import services.api.UserFeatureFlagApiService;

import javax.inject.Inject;
import java.util.UUID;

public class UserFeatureFlagApiServiceImpl
        extends AbstractApiService<UserFeatureFlag, UUID, UserFeatureFlagCriteria, UserFeatureFlagService, dto.UserFeatureFlag>
        implements UserFeatureFlagApiService {

  @Inject
  public UserFeatureFlagApiServiceImpl(UserFeatureFlagService service, PermissionService permissionService) {
    super(
            service,
            dto.UserFeatureFlag.class,
            UserFeatureFlagMapper::toDto,
            new Scope[]{Scope.FeatureFlagRead},
            new Scope[]{Scope.FeatureFlagWrite},
            permissionService);
  }

  @Override
  protected UserFeatureFlag toModel(JsonNode json) {
    return UserFeatureFlagMapper.toModel(Json.fromJson(json, dto.UserFeatureFlag.class));
  }
}
