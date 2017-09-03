package services.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import criterias.UserCriteria;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Scope;
import models.User;
import play.libs.Json;
import services.PermissionService;
import services.UserService;
import services.api.UserApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class UserApiServiceImpl extends
    AbstractApiService<User, UUID, UserCriteria, UserService, dto.User>
    implements UserApiService {

  @Inject
  protected UserApiServiceImpl(UserService userService, PermissionService permissionService) {
    super(userService, dto.User.class, dto.User::from,
        new Scope[]{Scope.UserRead},
        new Scope[]{Scope.UserWrite},
        permissionService);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected User toModel(JsonNode json) {
    dto.User dto = Json.fromJson(json, dto.User.class);

    return dto.toModel(service.byId(dto.id));
  }
}
