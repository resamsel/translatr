package criterias;

import com.avaje.ebean.ExpressionList;
import forms.SearchForm;
import play.mvc.Http;
import utils.JsonUtils;

public class UserFeatureFlagCriteria extends AbstractSearchCriteria<UserFeatureFlagCriteria> {

  private String feature;

  public UserFeatureFlagCriteria() {
    super("userFeatureFlag");
  }

  public static UserFeatureFlagCriteria from(Http.Request request) {
    return new UserFeatureFlagCriteria()
        .with(request)
        .withUserId(JsonUtils.getUuid(request.getQueryString("userId")))
        .withFeature(request.getQueryString("feature"));
  }

  public String getFeature() {
    return feature;
  }

  public void setFeature(String feature) {
    this.feature = feature;
  }

  public UserFeatureFlagCriteria withFeature(String feature) {
    setFeature(feature);
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
    query.order("feature");

    return super.paged(query);
  }

  @Override
  protected String getCacheKeyParticle() {
    return "";
  }
}
