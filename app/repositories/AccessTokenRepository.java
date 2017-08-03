package repositories;

import com.google.inject.ImplementedBy;
import criterias.AccessTokenCriteria;
import java.util.List;
import java.util.UUID;
import models.AccessToken;
import models.Key;
import models.Project;
import repositories.impl.AccessTokenRepositoryImpl;

@ImplementedBy(AccessTokenRepositoryImpl.class)
public interface AccessTokenRepository extends
    ModelRepository<AccessToken, Long, AccessTokenCriteria> {

  AccessToken byKey(String key);

  AccessToken byUserAndName(UUID userId, String name);
}
