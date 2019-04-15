package services.api;

import com.google.inject.ImplementedBy;
import criterias.AccessTokenCriteria;
import dto.AccessToken;
import services.api.impl.AccessTokenApiServiceImpl;

@ImplementedBy(AccessTokenApiServiceImpl.class)
public interface AccessTokenApiService extends ApiService<AccessToken, Long, AccessTokenCriteria> {
}
