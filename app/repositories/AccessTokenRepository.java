package repositories;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import criterias.AccessTokenCriteria;
import models.AccessToken;
import repositories.impl.AccessTokenRepositoryImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ImplementedBy(AccessTokenRepositoryImpl.class)
public interface AccessTokenRepository extends
    ModelRepository<AccessToken, Long, AccessTokenCriteria> {

  String FETCH_USER = "user";

  Map<String, List<String>> FETCH_MAP = ImmutableMap.of(FETCH_USER, Collections.singletonList("user"));

  String[] PROPERTIES_TO_FETCH = new String[]{FETCH_USER};

  AccessToken byKey(String key);

  AccessToken byUserAndName(UUID userId, String name);
}
