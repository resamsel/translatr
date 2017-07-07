package dto;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import criterias.LogEntryCriteria;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.LogEntry;
import org.joda.time.DateTime;
import play.i18n.Messages;
import play.mvc.Http.Context;
import utils.FormatUtils;

/**
 * @author resamsel
 * @version 23 May 2017
 */
public class AggregatedNotification extends Dto {
  private static final long serialVersionUID = -6020395415666005155L;

  public DateTime whenCreated;
  public String created;
  public DateTime whenUpdated;
  public String updated;
  public String title;
  public String subtitle;
  public String verb;
  public long activityCount;
  public long actorCount;
  public String contentType;
  public String name;
  public String icon;
  public String color;
  public String link;
  public User user;
  public Project project;

  public static AggregatedNotification from(AggregatedActivity<SimpleActivity> in,
      Map<UUID, LogEntry> logEntryMap) {
    Messages messages = Context.current().messages();

    AggregatedNotification out = new AggregatedNotification();

    out.whenCreated = new DateTime(in.getCreatedAt());
    out.created = FormatUtils.pretty(Context.current().lang().locale(), out.whenCreated);
    out.whenUpdated = new DateTime(in.getUpdatedAt());
    out.updated = FormatUtils.pretty(Context.current().lang().locale(), out.whenUpdated);
    out.verb = in.getVerb();
    out.activityCount = in.getActivityCount();
    out.actorCount = in.getActorCount();
    if (in.getActivities() != null && !in.getActivities().isEmpty()) {
      SimpleActivity firstActivity = in.getActivities().get(0);
      Notification activity = null;
      if (logEntryMap != null) {
        UUID logEntryId = Notification.extractUuid(firstActivity.getForeignId());
        if (logEntryMap.containsKey(logEntryId))
          activity = Notification.from(firstActivity, logEntryMap.get(logEntryId));
      }
      if (activity == null)
        activity = Notification.from(firstActivity);

      out.title = activity.title;
      out.contentType = activity.contentType;
      out.name = activity.name;
      out.icon = activity.icon;
      out.color = activity.color;
      out.link = activity.link;
      out.user = activity.user;
      out.project = activity.project;
    }
    out.subtitle = messages.at("activity." + out.verb + ".subtitle", out.activityCount,
        out.actorCount, out.updated);

    return out;
  }

  public static List<AggregatedNotification> from(List<AggregatedActivity<SimpleActivity>> in) {
    List<UUID> ids = in.stream().flatMap(activity -> activity.getActivities().stream())
        .map(activity -> Notification.extractUuid(activity.getForeignId())).collect(toList());
    Map<UUID, LogEntry> map = LogEntry.findBy(new LogEntryCriteria().withIds(ids)).getList()
        .stream().collect(toMap(LogEntry::getId, a -> a));

    return in.stream().map(a -> from(a, map)).collect(toList());
  }
}
