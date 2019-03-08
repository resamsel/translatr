package dto;

import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class User extends Dto {
  private static final long serialVersionUID = -3130261034824279541L;

  public UUID id;

  public DateTime whenCreated;

  public DateTime whenUpdated;

  public String name;

  public String username;

  public String email;

  public List<ProjectUser> memberships;

  /**
   * 
   */
  public User() {}

  private User(models.User in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.name = in.name;
    this.username = in.username;
    this.email = in.email;

    if (in.memberships != null && !in.memberships.isEmpty()) {
      this.memberships = in.memberships.stream().map(ProjectUser::from).collect(Collectors.toList());
    }
  }

  public models.User toModel(models.User user) {
    models.User out = user != null ? user : new models.User();

    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.name = name;
    out.username = username;
    out.email = email;

    return out;
  }

  /**
   * @param in
   * @return
   */
  public static User from(models.User in) {
    if (in == null) {
      return null;
    }

    return new User(in);
  }
}
