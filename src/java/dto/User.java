package dto;

import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class User extends Dto {
  private static final long serialVersionUID = -3130261034824279541L;

  public UUID id;

  public DateTime whenCreated;

  public DateTime whenUpdated;

  public String name;

  public String username;

  public String email;

  public UserRole role;

  public List<ProjectUser> memberships;
}
