package dto;

import java.util.Date;
import java.util.List;

import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;

/**
 * @author resamsel
 * @version 23 May 2017
 */
public class AggregatedNotification extends Dto {
  private static final long serialVersionUID = -6020395415666005155L;

  public Date createdAt;
  public Date updatedAt;
  public String verb;
  public long activityCount;
  public long actorCount;
  public List<Notification> activites;
  public String text;

  public static AggregatedNotification from(AggregatedActivity<SimpleActivity> in) {
    AggregatedNotification out = new AggregatedNotification();

    out.createdAt = in.getCreatedAt();
    out.updatedAt = in.getUpdatedAt();
    out.verb = in.getVerb();
    out.activityCount = in.getActivityCount();
    out.actorCount = in.getActorCount();
    if (in.getActivities() != null) {
      out.activites = Notification.fromList(in.getActivities());
      if (out.activites.size() > 0)
        out.text = out.activites.get(0).text;
    }

    return out;
  }
}
