package dto;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.User;

public class AccessToken extends Dto {
  public UUID userId;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  public String name;

  public String key;

  public String scope;

  private AccessToken(models.AccessToken in) {
    this.userId = in.user.id;
    this.name = in.name;
    this.key = in.key;
    this.scope = in.scope;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
  }

  public models.AccessToken toModel() {
    models.AccessToken out = new models.AccessToken();

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
