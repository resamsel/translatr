package services.api.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import criterias.UserCriteria;
import models.Scope;
import models.User;
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
    super(userService, dto.User.class, dto.User::from, User::from, new Scope[] {Scope.UserRead},
        new Scope[] {Scope.UserWrite});
  }
}
