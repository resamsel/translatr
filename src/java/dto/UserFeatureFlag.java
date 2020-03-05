package dto;

import play.data.validation.Constraints;

import java.util.UUID;

public class UserFeatureFlag extends Dto {
  private static final long serialVersionUID = 8641212952047519698L;

  public UUID id;

  @Constraints.Required
  public UUID userId;
  public String userUsername;
  public String userName;

  @Constraints.Required
  public Feature feature;
  @Constraints.Required
  public boolean enabled;
}
