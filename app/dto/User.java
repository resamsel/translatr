package dto;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User extends Dto {
  private static final long serialVersionUID = -3130261034824279541L;

  public UUID id;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  public String name;

  public String username;

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
  }

  public models.User toModel(models.User user) {
    models.User out = user != null ? user : new models.User();

    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.name = name;
    out.username = username;

    return out;
  }

  /**
   * @param project
   * @return
   */
  public static User from(models.User in) {
    return new User(in);
  }
}
