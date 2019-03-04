package services.api;

import com.avaje.ebean.PagedList;
import com.google.inject.ImplementedBy;
import criterias.UserCriteria;
import dto.Aggregate;
import dto.User;
import java.util.UUID;
import services.api.impl.UserApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(UserApiServiceImpl.class)
public interface UserApiService extends ApiService<User, UUID, UserCriteria> {
    User byUsername(String username, String... propertiesToFetch);

    PagedList<Aggregate> activity(UUID userId);

    User me();
}
