package services.api.impl;

import criterias.UserFeatureFlagCriteria;
import mappers.UserFeatureFlagMapper;
import models.Scope;
import models.UserFeatureFlag;
import services.PermissionService;
import services.UserFeatureFlagService;
import services.api.UserFeatureFlagApiService;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.UUID;

public class UserFeatureFlagApiServiceImpl
    extends AbstractApiService<UserFeatureFlag, UUID, UserFeatureFlagCriteria, UserFeatureFlagService, dto.UserFeatureFlag>
    implements UserFeatureFlagApiService {

  @Inject
  public UserFeatureFlagApiServiceImpl(UserFeatureFlagService service, PermissionService permissionService, Validator validator) {
    super(
        service,
        dto.UserFeatureFlag.class,
        UserFeatureFlagMapper::toDto,
        new Scope[]{Scope.FeatureFlagRead},
        new Scope[]{Scope.FeatureFlagWrite},
        permissionService,
        validator
    );
  }

  @Override
  protected UserFeatureFlag toModel(dto.UserFeatureFlag dto) {
    return UserFeatureFlagMapper.toModel(dto);
  }
}
