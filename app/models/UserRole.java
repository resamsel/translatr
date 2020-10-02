package models;

/**
 * @author resamsel
 * @version 3 Oct 2016
 */
public enum UserRole {
  Admin("admin"),
  User("user");

  private final String name;

  UserRole(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
