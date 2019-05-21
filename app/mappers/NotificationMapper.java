package mappers;

import dto.Notification;
import io.getstream.client.model.activities.SimpleActivity;
import models.LogEntry;
import play.api.Play;
import play.api.inject.Injector;
import services.LogEntryService;
import services.UserService;
import utils.ActivityUtils;

import java.util.Date;

public class NotificationMapper {
  /**
   *
   */
  public static Notification toDto(SimpleActivity in) {
    return toDto(in, null);
  }

  public static Notification toDto(SimpleActivity in, LogEntry activity) {
    Injector injector = Play.current().injector();
    UserService userService = injector.instanceOf(UserService.class);
    LogEntryService logEntryService = injector.instanceOf(LogEntryService.class);

    Notification out = new Notification();

    out.id = in.getId();
    out.user = UserMapper.toDto(userService.byId(Notification.extractUuid(in.getActor())));
    out.verb = in.getVerb();
    out.time = in.getTime() != null ? new Date(in.getTime().getTime()) : null;
    if (activity == null)
      activity = logEntryService.byId(Notification.extractUuid(in.getForeignId()));
    if (activity != null) {
      out.activityId = activity.id;
      out.contentType = activity.getSimpleContentType();
      out.title = ActivityUtils.titleOf(activity);
      out.name = ActivityUtils.nameOf(activity);
      out.icon = ActivityUtils.iconOf(activity);
      out.color = ActivityUtils.colorOf(activity);
      out.link = ActivityUtils.linkTo(activity).url();
      if (activity.project != null)
        out.project = ProjectMapper.toDto(activity.project);
    }

    return out;
  }
}
