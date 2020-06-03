package services.api;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.ImplementedBy;
import criterias.UserCriteria;
import dto.Aggregate;
import dto.User;
import services.api.impl.UserApiServiceImpl;

import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(UserApiServiceImpl.class)
public interface UserApiService extends ApiService<User, UUID, UserCriteria> {
  User byUsername(String username, String... propertiesToFetch);

  PagedList<Aggregate> activity(UUID userId);

  User me(String... propertiesToFetch);

  /**
   * Replace given settings of the user.
   */
  User saveSettings(UUID userId, JsonNode json);

  /**
   * Add or update given settings of the user.
   */
  User updateSettings(UUID userId, JsonNode json);
}
