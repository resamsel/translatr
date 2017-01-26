package validators;

import models.Locale;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
public class LocaleNameUniqueChecker implements NameUniqueChecker {
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof Locale))
      return false;

    Locale t = (Locale) o;

    return Locale.byProjectAndName(t.project, t.name) == null;
  }
}
