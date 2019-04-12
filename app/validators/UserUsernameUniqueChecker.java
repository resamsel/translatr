package validators;

import models.Key;
import models.User;
import repositories.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class UserUsernameUniqueChecker implements NameUniqueChecker {

  private final UserRepository userRepository;

  @Inject
  public UserUsernameUniqueChecker(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof User)) {
      return false;
    }

    User t = (User) o;
    User existing = userRepository.byUsername(t.username);

    return existing == null || existing.equals(t);
  }
}
