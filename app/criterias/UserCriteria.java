package criterias;

import forms.SearchForm;
import play.mvc.Http.Request;

/**
 * @author resamsel
 * @version 24 Jan 2017
 */
public class UserCriteria extends AbstractSearchCriteria<UserCriteria> {

  public UserCriteria() {
    super("user");
  }

  public static UserCriteria from(Request request) {
    return new UserCriteria().with(request);
  }

  /**
   * @param form
   * @return
   */
  public static UserCriteria from(SearchForm form) {
    return new UserCriteria().with(form);
  }
}
