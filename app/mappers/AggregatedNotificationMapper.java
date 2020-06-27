package mappers;

import criterias.LogEntryCriteria;
import dto.AggregatedNotification;
import dto.Notification;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import models.LogEntry;
import org.joda.time.DateTime;
import play.api.Play;
import play.i18n.Messages;
import play.mvc.Http;
import services.LogEntryService;
import utils.FormatUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class AggregatedNotificationMapper {
  public static AggregatedNotification toDto(AggregatedActivity<SimpleActivity> in,
                                             Map<UUID, LogEntry> logEntryMap) {
    Messages messages = Http.Context.current().messages();

    AggregatedNotification out = new AggregatedNotification();

    out.whenCreated = new DateTime(in.getCreatedAt());
    out.created = FormatUtils.pretty(Http.Context.current().lang().locale(), out.whenCreated);
    out.whenUpdated = new DateTime(in.getUpdatedAt());
    out.updated = FormatUtils.pretty(Http.Context.current().lang().locale(), out.whenUpdated);
    out.verb = in.getVerb();
    out.activityCount = in.getActivityCount();
    out.actorCount = in.getActorCount();
    if (in.getActivities() != null && !in.getActivities().isEmpty()) {
      SimpleActivity firstActivity = in.getActivities().get(0);
      Notification activity = null;
      if (logEntryMap != null) {
        UUID logEntryId = Notification.extractUuid(firstActivity.getForeignId());
        if (logEntryMap.containsKey(logEntryId)) {
          activity = NotificationMapper.toDto(firstActivity, logEntryMap.get(logEntryId));
        }
      }
      if (activity == null) {
        activity = NotificationMapper.toDto(firstActivity);
      }

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

  public static List<AggregatedNotification> toDto(List<AggregatedActivity<SimpleActivity>> in) {
    List<UUID> ids = in.stream().flatMap(activity -> activity.getActivities().stream())
        .map(activity -> Notification.extractUuid(activity.getForeignId())).collect(toList());
    Map<UUID, LogEntry> map = Play.current().injector().instanceOf(LogEntryService.class)
        .findBy(new LogEntryCriteria().withIds(ids)).getList()
        .stream().collect(toMap(LogEntry::getId, a -> a));

    return in.stream().map(a -> toDto(a, map)).collect(toList());
  }
}
