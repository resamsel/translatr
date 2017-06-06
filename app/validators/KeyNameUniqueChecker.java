package validators;

import models.Key;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
public class KeyNameUniqueChecker implements NameUniqueChecker {
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof Key))
      return false;

    Key t = (Key) o;

    return Key.byProjectAndName(t.project, t.name) == null;
  }
}
