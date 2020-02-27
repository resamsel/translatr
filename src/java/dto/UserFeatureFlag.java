package dto;

import org.joda.time.DateTime;

import java.util.UUID;

public class UserFeatureFlag extends Dto {
  private static final long serialVersionUID = 8641212952047519698L;

  public UUID id;

  public UUID userId;
  public String userUsername;
  public String userName;

  public DateTime whenCreated;
  public DateTime whenUpdated;

  public String featureFlag;
  public boolean enabled;
}
