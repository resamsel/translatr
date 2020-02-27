package mappers;

import dto.User;
import utils.EmailUtils;

import java.util.HashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class UserMapper {
  public static models.User toModel(User in, models.User user) {
    models.User out = user != null ? user : new models.User();

    out.name = in.name;
    out.username = in.username;
    if (in.email != null) {
      out.email = in.email;
    }
    if (in.role != null) {
      out.role = UserRoleMapper.toModel(in.role);
    }

    return out;
  }

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
    out.emailHash = EmailUtils.hashEmail(in.email);
    out.role = UserRoleMapper.toDto(in.role);

    if (in.memberships != null && !in.memberships.isEmpty()) {
      out.memberships = in.memberships.stream()
          .map(ProjectUserMapper::toDto)
          .collect(Collectors.toList());
    }

    if (in.featureFlags != null && !in.featureFlags.isEmpty()) {
      out.featureFlags = in.featureFlags.stream()
              .collect(toMap(ff -> ff.featureFlag.getName(), ff -> ff.enabled));
    }

    return out;
  }
}
