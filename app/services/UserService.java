package services;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.ImplementedBy;
import criterias.UserCriteria;
import models.User;
import models.UserStats;
import services.impl.UserServiceImpl;

import java.util.UUID;

/**
 *
 * @author resamsel
 * @version 1 Oct 2016
 */
@ImplementedBy(UserServiceImpl.class)
public interface UserService extends ModelService<User, UUID, UserCriteria> {
  /**
   * @param user
   * @return
   */
  User create(AuthUserIdentity user);

  /**
   * @param user
   * @param otherUser
   * @return
   */
  User addLinkedAccount(AuthUserIdentity user, AuthUserIdentity otherUser);

  /**
   * @param user
   */
  User getLocalUser(AuthUserIdentity user);

  /**
   * @param authUser
   */
  boolean isLocalUser(AuthUserIdentity authUser);

  /**
   * @param user
   * @param otherUser
   */
  User merge(AuthUserIdentity user, AuthUserIdentity otherUser);

  /**
   * @param user
   * @param otherUser
   * @return
   */
  User merge(User user, User otherUser);

  /**
   * @param authUser
   */
  void logout(AuthUserIdentity authUser);

  /**
   * @param username
   * @param fetches
   * @return
   */
  User byUsername(String username, String... fetches);

  /**
   * @param userId
   * @return
   */
  UserStats getUserStats(UUID userId);
}
