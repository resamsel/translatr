package repositories;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import criterias.UserCriteria;
import models.User;
import repositories.impl.UserRepositoryImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ImplementedBy(UserRepositoryImpl.class)
public interface UserRepository extends ModelRepository<User, UUID, UserCriteria> {

  String FETCH_PROJECTS = "projects";
  String FETCH_MEMBERSHIPS = "memberships";
  String FETCH_ACTIVITIES = "activities";
  String FETCH_FEATURES = "features";
  String FETCH_SETTINGS = "settings";

  String[] PROPERTIES_TO_FETCH = {};

  Map<String, List<String>> FETCH_MAP = ImmutableMap.of(
          FETCH_PROJECTS, Collections.singletonList(FETCH_PROJECTS),
          FETCH_FEATURES, Collections.singletonList(FETCH_FEATURES),
          FETCH_SETTINGS, Collections.singletonList(FETCH_SETTINGS)
  );

  User byUsername(String username, String... fetches);

  User findByAuthUserIdentity(AuthUserIdentity identity);

  String nameToUsername(String name);

  String emailToUsername(String email);

  String uniqueUsername(String username);

  /**
   * Replace user settings with given settings.
   */
  User saveSettings(UUID userId, Map<String, String> settings);

  /**
   * Add to or update user settings with given settings.
   */
  User updateSettings(UUID userId, Map<String, String> settings);
}
