package validators;

import models.AccessToken;
import repositories.AccessTokenRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class AccessTokenNameUniqueChecker implements NameUniqueChecker {

  private final AccessTokenRepository accessTokenRepository;

  @Inject
  public AccessTokenNameUniqueChecker(AccessTokenRepository accessTokenRepository) {
    this.accessTokenRepository = accessTokenRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof AccessToken)) {
      return false;
    }

    AccessToken t = (AccessToken) o;
    AccessToken existing = accessTokenRepository.byUserAndName(t.user.id, t.name);

    return existing == null || t.id == existing.id;
  }
}
