package services.api;

import com.google.inject.ImplementedBy;
import criterias.UserFeatureFlagCriteria;
import dto.UserFeatureFlag;
import services.api.impl.UserFeatureFlagApiServiceImpl;

import java.util.UUID;

@ImplementedBy(UserFeatureFlagApiServiceImpl.class)
public interface UserFeatureFlagApiService extends ApiService<UserFeatureFlag, UUID, UserFeatureFlagCriteria> {
}
