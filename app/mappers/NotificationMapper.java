package mappers;

import dto.Notification;
import io.getstream.client.model.activities.SimpleActivity;
import models.LogEntry;
import play.mvc.Http;
import services.LogEntryService;
import services.UserService;
import utils.ActivityUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class NotificationMapper {
  private final UserService userService;
  private final LogEntryService logEntryService;
  private final ActivityUtils activityUtils;
  private final ProjectMapper projectMapper;

  @Inject
  public NotificationMapper(UserService userService, LogEntryService logEntryService, ActivityUtils activityUtils, ProjectMapper projectMapper) {
    this.userService = userService;
    this.logEntryService = logEntryService;
    this.activityUtils = activityUtils;
    this.projectMapper = projectMapper;
  }

  public Notification toDto(SimpleActivity in, Http.Request request) {
    return toDto(in, null, request);
  }

  public Notification toDto(SimpleActivity in, LogEntry activity, Http.Request request) {
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
      out.title = activityUtils.titleOf(activity, request);
      out.name = ActivityUtils.nameOf(activity);
      out.icon = ActivityUtils.iconOf(activity);
      out.color = ActivityUtils.colorOf(activity);
      if (activity.project != null)
        out.project = projectMapper.toDto(activity.project, request);
    }

    return out;
  }
}
