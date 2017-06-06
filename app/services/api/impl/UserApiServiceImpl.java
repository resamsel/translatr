package services.api.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

import criterias.UserCriteria;
import models.Scope;
import models.User;
import play.libs.Json;
import services.UserService;
import services.api.UserApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class UserApiServiceImpl extends AbstractApiService<User, UUID, UserCriteria, dto.User>
    implements UserApiService {
  /**
   * @param userService
   */
  @Inject
  protected UserApiServiceImpl(UserService userService) {
    super(userService, dto.User.class, dto.User::from, new Scope[] {Scope.UserRead},
        new Scope[] {Scope.UserWrite});
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
