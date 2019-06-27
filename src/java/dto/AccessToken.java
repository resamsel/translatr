package dto;

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
}
