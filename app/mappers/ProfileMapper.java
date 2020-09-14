package mappers;

import dto.Profile;
import dto.UserRole;
import org.pac4j.core.profile.CommonProfile;

public class ProfileMapper {
  public static Profile toDto(CommonProfile profile) {
    Profile out = new Profile();

    out.clientName = profile.getClientName();
    out.name = profile.getDisplayName();
    out.username = profile.getUsername();
    out.email = profile.getEmail();
    if (profile.getLocale() != null) {
      out.preferredLanguage = profile.getLocale().toString();
    }
    out.role = profile.getRoles().contains("admin") ? UserRole.Admin : UserRole.User;

    return out;
  }
}
