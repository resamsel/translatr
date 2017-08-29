package criterias;

import play.mvc.Http.Request;

/**
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class NotificationCriteria extends AbstractSearchCriteria<NotificationCriteria> {

  private NotificationCriteria() {
    super("notification");
  }

  public static NotificationCriteria from(Request request) {
    return new NotificationCriteria().with(request);
  }

  @Override
  protected String getCacheKeyParticle() {
    return "";
  }
}
