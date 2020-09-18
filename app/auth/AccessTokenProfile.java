package auth;

import models.Scope;
import org.pac4j.core.profile.CommonProfile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AccessTokenProfile extends CommonProfile {
  private static final String SCOPE = "scope";

  public void setScope(String scope) {
    addAttribute(SCOPE, scope);
  }

  public List<Scope> getScope() {
    return Arrays.stream(((String) getAttribute(SCOPE)).split(","))
            .map(Scope::fromString)
            .collect(Collectors.toList());
  }
}
