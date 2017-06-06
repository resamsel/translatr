package criterias;

import play.mvc.Http.Request;

/**
 * @author resamsel
 * @version 24 Jan 2017
 */
public class UserCriteria extends AbstractSearchCriteria<UserCriteria> {
  public static UserCriteria from(Request request) {
    return new UserCriteria().with(request);
  }
}
