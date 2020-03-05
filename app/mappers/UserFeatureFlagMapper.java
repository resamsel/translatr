package mappers;

import dto.UserFeatureFlag;
import models.User;

public class UserFeatureFlagMapper {
  public static models.UserFeatureFlag toModel(UserFeatureFlag in) {
    if (in == null) {
      return null;
    }

    models.UserFeatureFlag out = new models.UserFeatureFlag();

    out.id = in.id;
    out.user = new User().withId(in.userId);
    out.feature = FeatureMapper.toModel(in.feature);
    out.enabled = in.enabled;

    return out;
  }

  public static UserFeatureFlag toDto(models.UserFeatureFlag in) {
    UserFeatureFlag out = new UserFeatureFlag();

    out.id = in.id;
    out.userId = in.user.id;
    out.userUsername = in.user.username;
    out.userName = in.user.name;
    out.feature = FeatureMapper.toDto(in.feature);
    out.enabled = in.enabled;

    return out;
  }
}
