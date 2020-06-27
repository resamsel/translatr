package models;

import be.objectify.deadbolt.java.models.Role;

/**
 * @author resamsel
 * @version 3 Oct 2016
 */
public enum UserRole implements Role {
  Admin("admin"),
  User("user");

  private String name;

  UserRole(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
