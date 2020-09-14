package mappers;

import dto.Profile;
import dto.UserRole;
import org.pac4j.oidc.profile.OidcProfile;

public class ProfileMapper {
  public static Profile toDto(OidcProfile profile) {
    Profile out = new Profile();

    out.clientName = profile.getClientName();
    out.name = profile.getDisplayName();
    out.username = profile.getUsername();
    out.email = profile.getEmail();
    out.preferredLanguage = profile.getLocale().toString();
    out.role = profile.getRoles().contains("admin") ? UserRole.Admin : UserRole.User;

    return out;
  }
}
