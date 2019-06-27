package mappers;

import dto.UserRole;

public class UserRoleMapper {
  public static UserRole toDto(models.UserRole role) {
    return UserRole.valueOf(role.name());
  }

  public static models.UserRole toModel(UserRole in) {
    return models.UserRole.valueOf(in.name());
  }
}
