package dto;

import java.util.UUID;

public class UserFeatureFlag extends Dto {
  private static final long serialVersionUID = 8641212952047519698L;

  public UUID id;

  public UUID userId;
  public String userUsername;
  public String userName;

  public String featureFlag;
  public boolean enabled;
}
