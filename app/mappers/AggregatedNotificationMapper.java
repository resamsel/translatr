package mappers;

import criterias.LogEntryCriteria;
import dto.AggregatedNotification;
import dto.Notification;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import models.LogEntry;
import org.joda.time.DateTime;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Http;
import services.LogEntryService;
import utils.FormatUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Singleton
public class AggregatedNotificationMapper {
  private final LogEntryService logEntryService;
  private final NotificationMapper notificationMapper;
  private final MessagesApi messagesApi;

  @Inject
  public AggregatedNotificationMapper(LogEntryService logEntryService, NotificationMapper notificationMapper, MessagesApi messagesApi) {
    this.logEntryService = logEntryService;
    this.notificationMapper = notificationMapper;
    this.messagesApi = messagesApi;
  }

  public AggregatedNotification toDto(AggregatedActivity<SimpleActivity> in,
                                      Map<UUID, LogEntry> logEntryMap, Http.Request request) {
    Messages messages = messagesApi.preferred(request);
    Locale locale = messages.lang().locale();

    AggregatedNotification out = new AggregatedNotification();

    out.whenCreated = new DateTime(in.getCreatedAt());
    out.created = FormatUtils.pretty(locale, out.whenCreated);
    out.whenUpdated = new DateTime(in.getUpdatedAt());
    out.updated = FormatUtils.pretty(locale, out.whenUpdated);
    out.verb = in.getVerb();
    out.activityCount = in.getActivityCount();
    out.actorCount = in.getActorCount();
    if (in.getActivities() != null && !in.getActivities().isEmpty()) {
      SimpleActivity firstActivity = in.getActivities().get(0);
      Notification activity = null;
      if (logEntryMap != null) {
        UUID logEntryId = Notification.extractUuid(firstActivity.getForeignId());
        if (logEntryMap.containsKey(logEntryId)) {
          activity = notificationMapper.toDto(firstActivity, logEntryMap.get(logEntryId), request);
        }
      }
      if (activity == null) {
        activity = notificationMapper.toDto(firstActivity, request);
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

  public List<AggregatedNotification> toDto(List<AggregatedActivity<SimpleActivity>> in, Http.Request request) {
    List<UUID> ids = in.stream().flatMap(activity -> activity.getActivities().stream())
            .map(activity -> Notification.extractUuid(activity.getForeignId())).collect(toList());
    Map<UUID, LogEntry> map = logEntryService
            .findBy(new LogEntryCriteria().withIds(ids)).getList()
            .stream().collect(toMap(LogEntry::getId, a -> a));

    return in.stream().map(a -> toDto(a, map, request)).collect(toList());
  }
}
