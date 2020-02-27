package mappers;

import dto.UserFeatureFlag;
import models.FeatureFlag;
import models.User;

public class UserFeatureFlagMapper {
  public static models.UserFeatureFlag toModel(UserFeatureFlag in) {
    if (in == null) {
      return null;
    }

    models.UserFeatureFlag out = new models.UserFeatureFlag();

    out.id = in.id;
    out.user = new User().withId(in.userId);
    out.featureFlag = FeatureFlag.of(in.featureFlag);
    out.enabled = in.enabled;

    return out;
  }

  public static UserFeatureFlag toDto(models.UserFeatureFlag in) {
    UserFeatureFlag out = new UserFeatureFlag();

    out.id = in.id;
    out.userId = in.user.id;
    out.userUsername = in.user.username;
    out.userName = in.user.name;
    out.featureFlag = in.featureFlag.getName();
    out.enabled = in.enabled;

    return out;
  }
}
