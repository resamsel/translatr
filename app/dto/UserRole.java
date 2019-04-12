package dto;

public enum UserRole {
  Admin, User;

  public static UserRole from(models.UserRole role) {
    return UserRole.valueOf(role.name());
  }

  public models.UserRole toModel() {
    return models.UserRole.valueOf(name());
  }
}
