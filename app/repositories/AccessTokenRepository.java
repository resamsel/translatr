package repositories;

import com.google.inject.ImplementedBy;
import criterias.AccessTokenCriteria;
import models.AccessToken;
import repositories.impl.AccessTokenRepositoryImpl;

import java.util.UUID;

@ImplementedBy(AccessTokenRepositoryImpl.class)
public interface AccessTokenRepository extends
    ModelRepository<AccessToken, Long, AccessTokenCriteria> {

  String FETCH_USER = "user";

  String[] PROPERTIES_TO_FETCH = new String[]{FETCH_USER};

  AccessToken byKey(String key);

  AccessToken byUserAndName(UUID userId, String name);
}
