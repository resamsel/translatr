package validators;

import javax.inject.Inject;
import javax.inject.Singleton;
import models.Key;
import repositories.KeyRepository;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class KeyNameUniqueChecker implements NameUniqueChecker {

  private final KeyRepository keyRepository;

  @Inject
  public KeyNameUniqueChecker(KeyRepository keyRepository) {
    this.keyRepository = keyRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof Key)) {
      return false;
    }

    Key t = (Key) o;
    Key existing = keyRepository.byProjectAndName(t.project, t.name);

    return existing == null || existing.equals(t);
  }
}
