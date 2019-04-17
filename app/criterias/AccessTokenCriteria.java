package criterias;

import com.avaje.ebean.ExpressionList;
import forms.SearchForm;
import play.mvc.Http;
import utils.JsonUtils;

/**
 *
 * @author resamsel
 * @version 19 Oct 2016
 */
public class AccessTokenCriteria extends AbstractSearchCriteria<AccessTokenCriteria> {

  public AccessTokenCriteria() {
    super("accessToken");
  }

  public static AccessTokenCriteria from(Http.Request request) {
    return new AccessTokenCriteria()
        .with(request)
        .withUserId(JsonUtils.getUuid(request.getQueryString("userId")));
  }

  public static AccessTokenCriteria from(SearchForm form) {
    return new AccessTokenCriteria().with(form);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <U> ExpressionList<U> paged(ExpressionList<U> query) {
    query.order("whenCreated");

    return super.paged(query);
  }

  @Override
  protected String getCacheKeyParticle() {
    return "";
  }
}
