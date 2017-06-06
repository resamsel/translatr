package dto;

import java.util.Date;
import java.util.UUID;

import io.getstream.client.model.activities.SimpleActivity;
import models.LogEntry;
import play.api.Play;
import play.api.inject.Injector;
import services.LogEntryService;
import services.UserService;
import utils.ActivityUtils;
import utils.JsonUtils;

/**
 * @author resamsel
 * @version 23 May 2017
 */
public class Notification extends Dto {
  private static final long serialVersionUID = -2532807699451690607L;

  public String id;
  public Date time;
  public User user;
  public Project project;
  public String verb;
  public String title;
  public UUID activityId;
  public String contentType;
  public String name;
  public String icon;
  public String color;
  public String link;

  /**
   * 
   */
  public static Notification from(SimpleActivity in) {
    return from(in, null);
  }

  public static Notification from(SimpleActivity in, LogEntry activity) {
    Injector injector = Play.current().injector();
    UserService userService = injector.instanceOf(UserService.class);
    LogEntryService logEntryService = injector.instanceOf(LogEntryService.class);

    Notification out = new Notification();

    out.id = in.getId();
    out.user = User.from(userService.byId(extractUuid(in.getActor())));
    out.verb = in.getVerb();
    out.time = in.getTime();
    if (activity == null)
      activity = logEntryService.byId(extractUuid(in.getForeignId()));
    if (activity != null) {
      out.activityId = activity.id;
      out.contentType = activity.getSimpleContentType();
      out.title = ActivityUtils.titleOf(activity);
      out.name = ActivityUtils.nameOf(activity);
      out.icon = ActivityUtils.iconOf(activity);
      out.color = ActivityUtils.colorOf(activity);
      out.link = ActivityUtils.linkTo(activity).url();
      if (activity.project != null)
        out.project = Project.from(activity.project);
    }

    return out;
  }

  public static UUID extractUuid(String id) {
    String[] parts = id.split(":", 2);
    if (parts.length != 2)
      return null;

    return JsonUtils.getUuid(parts[1]);
  }
}
