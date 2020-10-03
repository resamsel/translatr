package criterias;

import forms.SearchForm;
import play.mvc.Http;
import utils.JsonUtils;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LinkedAccountCriteria extends AbstractSearchCriteria<LinkedAccountCriteria> {

  public LinkedAccountCriteria() {
    super("linkedAccount");
  }

  public static LinkedAccountCriteria from(SearchForm form) {
    return new LinkedAccountCriteria().with(form);
  }

  public static LinkedAccountCriteria from(Http.Request request) {
    return new LinkedAccountCriteria()
            .with(request)
            .withUserId(JsonUtils.getUuid(request.getQueryString("userId")));
  }

  @Override
  protected String getCacheKeyParticle() {
    return "";
  }
}
