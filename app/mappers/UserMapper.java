package mappers;

import dto.User;

import java.util.stream.Collectors;

public class UserMapper {
  public static models.User toModel(User in, models.User user) {
    models.User out = user != null ? user : new models.User();

    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.name = in.name;
    out.username = in.username;
    out.email = in.email;
    out.role = UserRoleMapper.toModel(in.role);

    return out;
  }

  /**
   * @param in
   * @return
   */
  public static User toDto(models.User in) {
    if (in == null) {
      return null;
    }

    User out = new User();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.name = in.name;
    out.username = in.username;
    out.email = in.email;
    out.role = UserRoleMapper.toDto(in.role);

    if (in.memberships != null && !in.memberships.isEmpty()) {
      out.memberships = in.memberships.stream()
          .map(ProjectUserMapper::toDto)
          .collect(Collectors.toList());
    }

    return out;
  }
}
