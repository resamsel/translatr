package mappers;

import dto.User;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

import static utils.EmailUtils.maskEmail;

public class UserObfuscatorMapper implements Function<User, User> {
  private final models.User loggedInUser;

  private UserObfuscatorMapper(models.User loggedInUser) {
    this.loggedInUser = loggedInUser;
  }

  public static Function<User, User> of(models.User loggedInUser) {
    return new UserObfuscatorMapper(loggedInUser);
  }

  @Override
  public User apply(User in) {
    if (loggedInUser != null
        && !loggedInUser.isAdmin()
        && !loggedInUser.id.equals(in.id)
        && StringUtils.isNotEmpty(in.email)) {
      in.email = maskEmail(in.email);
    }

    return in;
  }
}
