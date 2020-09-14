package services;

import com.google.inject.ImplementedBy;
import criterias.UserCriteria;
import models.User;
import models.UserStats;
import services.impl.UserServiceImpl;

import java.util.Map;
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
   * @param otherUser
   * @return
   */
  User merge(User user, User otherUser);

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

  /**
   * Replace user settings with given settings.
   */
  User saveSettings(UUID userId, Map<String, String> settings);

  /**
   * Add to or update user settings with given settings.
   */
  User updateSettings(UUID userId, Map<String, String> settings);

  User byLinkedAccount(String providerKey, String providerUserId);

  User byAccessToken(String accessTokenKey);
}
