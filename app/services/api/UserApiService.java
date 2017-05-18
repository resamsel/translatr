package services.api;

import java.util.UUID;

import com.google.inject.ImplementedBy;

import criterias.UserCriteria;
import dto.User;
import services.api.impl.UserApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(UserApiServiceImpl.class)
public interface UserApiService extends ApiService<User, UUID, UserCriteria> {

}
