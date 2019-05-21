package services.impl;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.service.AbstractUserService;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.UserRoleIdentity;
import mappers.UserMapper;
import models.ActionType;
import models.LogEntry;
import models.User;
import services.LogEntryService;
import services.UserService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthenticateServiceImpl extends AbstractUserService {
  private final UserService userService;
  private final LogEntryService logEntryService;

  @Inject
  public AuthenticateServiceImpl(final PlayAuthenticate auth, final UserService userService,
      final LogEntryService logEntryService) {
    super(auth);
    this.userService = userService;
    this.logEntryService = logEntryService;
  }

  @Override
  public Object save(final AuthUser authUser) {
    final boolean isLinked = userService.isLocalUser(authUser);
    if (!isLinked)
      return userService.create(authUser).id;

    // we have this user already, so return null
    return null;
  }

  @Override
  public Object getLocalIdentity(final AuthUserIdentity identity) {
    // For production: Caching might be a good idea here...
    // ...and dont forget to sync the cache when users get deactivated/deleted
    final User u = userService.getLocalUser(identity);
    if (u != null)
      return u.id;
    else
      return null;
  }

  @Override
  public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
    if (!oldUser.equals(newUser))
      userService.merge(oldUser, newUser);

    return oldUser;
  }

  @Override
  public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
    userService.addLinkedAccount(oldUser, newUser);
    return oldUser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AuthUser update(AuthUser knownUser) {
    User user = userService.getLocalUser(knownUser);

    if (knownUser instanceof UserRoleIdentity) {
      UserRoleIdentity identity = (UserRoleIdentity) knownUser;
      if (user.role != identity.getUserRole()) {
        user = userService.update(user.withRole(identity.getUserRole()));
      }
    }

    logEntryService.create(
        LogEntry.from(ActionType.Login, user, null, dto.User.class, null, UserMapper.toDto(user)));

    return knownUser;
  }
}
