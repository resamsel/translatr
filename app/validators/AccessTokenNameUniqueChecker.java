package validators;

import models.AccessToken;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
public class AccessTokenNameUniqueChecker implements NameUniqueChecker {
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof AccessToken))
      return false;

    AccessToken t = (AccessToken) o;

    return AccessToken.byUserAndName(t.user.id, t.name) == null;
  }
}
