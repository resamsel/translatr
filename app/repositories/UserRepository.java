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

  String[] PROPERTIES_TO_FETCH = {};

  Map<String, List<String>> FETCH_MAP = ImmutableMap.of(
      FETCH_PROJECTS, Collections.singletonList(FETCH_PROJECTS));

  User byUsername(String username, String... fetches);

  User findByAuthUserIdentity(AuthUserIdentity identity);

  String nameToUsername(String name);

  String emailToUsername(String email);

  String uniqueUsername(String username);
}
