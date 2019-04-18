package dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.User;
import org.joda.time.DateTime;

import java.util.UUID;

public class AccessToken extends Dto {
  private static final long serialVersionUID = 8641212952047519698L;

  public Long id;

  public UUID userId;
  public String userName;

  public DateTime whenCreated;
  public DateTime whenUpdated;

  public String name;

  public String key;

  public String scope;

  public AccessToken() {
  }

  private AccessToken(models.AccessToken in) {
    this.id = in.id;
    this.userId = in.user.id;
    this.userName = in.user.name;
    this.name = in.name;
    this.key = in.key;
    this.scope = in.scope;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
  }

  public models.AccessToken toModel() {
    models.AccessToken out = new models.AccessToken();

    out.id = id;
    out.user = new User().withId(userId);
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.name = name;
    out.key = key;
    out.scope = scope;

    return out;
  }

  public static AccessToken from(models.AccessToken accessToken) {
    return new AccessToken(accessToken);
  }
}
