package models;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * @author resamsel
 * @version 3 Oct 2016
 */
public enum UserRole {
  Admin("admin"),
  User("user");

  private static final Map<String, UserRole> USER_ROLE_MAP = Arrays.stream(UserRole.values())
          .collect(toMap(UserRole::name, Function.identity()));

  private final String name;

  UserRole(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static UserRole fromString(String s) {
    return USER_ROLE_MAP.get(s);
  }
}
