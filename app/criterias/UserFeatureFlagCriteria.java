package criterias;

import com.avaje.ebean.ExpressionList;
import forms.SearchForm;
import play.mvc.Http;
import utils.JsonUtils;

public class UserFeatureFlagCriteria extends AbstractSearchCriteria<UserFeatureFlagCriteria> {

  public UserFeatureFlagCriteria() {
    super("userFeatureFlag");
  }

  public static UserFeatureFlagCriteria from(Http.Request request) {
    return new UserFeatureFlagCriteria()
            .with(request)
            .withUserId(JsonUtils.getUuid(request.getQueryString("userId")));
  }

  public static UserFeatureFlagCriteria from(SearchForm form) {
    return new UserFeatureFlagCriteria().with(form);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <U> ExpressionList<U> paged(ExpressionList<U> query) {
    query.order("featureFlag");

    return super.paged(query);
  }

  @Override
  protected String getCacheKeyParticle() {
    return "";
  }
}
