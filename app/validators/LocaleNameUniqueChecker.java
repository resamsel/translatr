package validators;

import javax.inject.Inject;
import javax.inject.Singleton;
import models.Locale;
import repositories.LocaleRepository;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class LocaleNameUniqueChecker implements NameUniqueChecker {

  private final LocaleRepository localeRepository;

  @Inject
  public LocaleNameUniqueChecker(LocaleRepository localeRepository) {
    this.localeRepository = localeRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof Locale)) {
      return false;
    }

    Locale t = (Locale) o;
    Locale existing = localeRepository.byProjectAndName(t.project, t.name);

    return existing == null || existing.equals(t);
  }
}
