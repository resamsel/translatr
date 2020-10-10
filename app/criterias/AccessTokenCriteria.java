package criterias;

import io.ebean.ExpressionList;
import models.UserRole;
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
        .withUserId(JsonUtils.getUuid(request.getQueryString("userId")))
        .withUserRole(request.queryString("userRole").map(UserRole::fromString).orElse(null));
  }

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
