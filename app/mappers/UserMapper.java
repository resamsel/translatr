package mappers;

import dto.User;
import utils.EmailUtils;

import java.util.HashMap;
import java.util.Locale;
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
    if (in.preferredLanguage != null) {
      out.preferredLocale = new Locale(in.preferredLanguage);
    }
    if (in.settings != null && !in.settings.isEmpty()) {
      out.settings = new HashMap<>(in.settings);
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

    if (in.preferredLocale != null) {
      out.preferredLanguage = in.preferredLocale.getLanguage();
    }

    if (in.memberships != null && !in.memberships.isEmpty()) {
      out.memberships = in.memberships.stream()
              .map(ProjectUserMapper::toDto)
              .collect(Collectors.toList());
    }

    if (in.features != null && !in.features.isEmpty()) {
      out.features = in.features.stream()
              .collect(toMap(ff -> ff.feature.getName(), ff -> ff.enabled));
    }

    if (in.settings != null && !in.settings.isEmpty()) {
      out.settings = new HashMap<>(in.settings);
    }

    return out;
  }
}
