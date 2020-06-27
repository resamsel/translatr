package services;

import com.google.inject.ImplementedBy;
import criterias.UserFeatureFlagCriteria;
import models.UserFeatureFlag;
import services.impl.UserFeatureFlagServiceImpl;

import java.util.UUID;

@ImplementedBy(UserFeatureFlagServiceImpl.class)
public interface UserFeatureFlagService extends ModelService<UserFeatureFlag, UUID, UserFeatureFlagCriteria> {
}
