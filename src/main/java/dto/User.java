package dto;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class User extends Dto {
  private static final long serialVersionUID = -3130261034824279541L;

  public UUID id;

  public DateTime whenCreated;

  public DateTime whenUpdated;

  public String name;

  public String username;

  public String email;
  public String emailHash;

  public UserRole role;

  public String preferredLanguage;

  public List<ProjectUser> memberships;

  public Map<String, Boolean> features;

  public Map<String, String> settings;
}
