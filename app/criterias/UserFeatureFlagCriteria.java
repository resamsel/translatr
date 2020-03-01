package criterias;

import com.avaje.ebean.ExpressionList;
import forms.SearchForm;
import play.mvc.Http;
import utils.JsonUtils;

public class UserFeatureFlagCriteria extends AbstractSearchCriteria<UserFeatureFlagCriteria> {

  private String featureFlag;

  public UserFeatureFlagCriteria() {
    super("userFeatureFlag");
  }

  public static UserFeatureFlagCriteria from(Http.Request request) {
    return new UserFeatureFlagCriteria()
        .with(request)
        .withUserId(JsonUtils.getUuid(request.getQueryString("userId")))
        .withFeatureFlag(request.getQueryString("featureFlag"));
  }

  public String getFeatureFlag() {
    return featureFlag;
  }

  public void setFeatureFlag(String featureFlag) {
    this.featureFlag = featureFlag;
  }

  public UserFeatureFlagCriteria withFeatureFlag(String featureFlag) {
    setFeatureFlag(featureFlag);
    return this;
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
