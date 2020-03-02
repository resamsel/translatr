package repositories;

import com.google.inject.ImplementedBy;
import criterias.UserFeatureFlagCriteria;
import models.FeatureFlag;
import models.UserFeatureFlag;
import repositories.impl.UserFeatureFlagRepositoryImpl;

import java.util.UUID;

@ImplementedBy(UserFeatureFlagRepositoryImpl.class)
public interface UserFeatureFlagRepository extends
        ModelRepository<UserFeatureFlag, UUID, UserFeatureFlagCriteria> {

  String FETCH_USER = "user";

  String[] PROPERTIES_TO_FETCH = new String[]{FETCH_USER};

  UserFeatureFlag byUserIdAndFeature(UUID id, FeatureFlag featureFlag);
}
