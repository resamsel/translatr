package services.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.ImplementedBy;
import criterias.UserCriteria;
import dto.Aggregate;
import dto.Profile;
import dto.User;
import io.ebean.PagedList;
import play.mvc.Http;
import services.api.impl.UserApiServiceImpl;

import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(UserApiServiceImpl.class)
public interface UserApiService extends ApiService<User, UUID, UserCriteria> {
  User create(Http.Request request);

  User byUsername(Http.Request request, String username, String... propertiesToFetch);

  PagedList<Aggregate> activity(Http.Request request, UUID userId);

  Profile profile(Http.Request request);

  User me(Http.Request request, String... propertiesToFetch);

  /**
   * Replace given settings of the user.
   */
  User saveSettings(UUID userId, JsonNode json, Http.Request request);

  /**
   * Add or update given settings of the user.
   */
  User updateSettings(UUID userId, JsonNode json, Http.Request request);
}
