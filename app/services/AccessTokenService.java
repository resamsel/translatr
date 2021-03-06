package services;

import com.google.inject.ImplementedBy;
import criterias.AccessTokenCriteria;
import models.AccessToken;
import play.mvc.Http;
import services.impl.AccessTokenServiceImpl;

/**
 *
 * @author resamsel
 * @version 19 Oct 2016
 */
@ImplementedBy(AccessTokenServiceImpl.class)
public interface AccessTokenService extends ModelService<AccessToken, Long, AccessTokenCriteria> {
  /**
   * @param accessTokenKey
   * @return
   */
  AccessToken byKey(String accessTokenKey, Http.Request request);
}
