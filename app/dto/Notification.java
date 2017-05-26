package dto;

import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.getstream.client.model.activities.SimpleActivity;
import models.LogEntry;
import play.api.Play;
import play.api.inject.Injector;
import play.i18n.Messages;
import play.mvc.Http.Context;
import services.LogEntryService;
import services.UserService;
import utils.FormatUtils;
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
  public String text;
  public UUID activityId;
  public String contentType;

  /**
   * 
   */
  public static Notification from(SimpleActivity in) {
    Injector injector = Play.current().injector();
    UserService userService = injector.instanceOf(UserService.class);
    LogEntryService logEntryService = injector.instanceOf(LogEntryService.class);

    Notification out = new Notification();

    out.id = in.getId();
    out.user = User.from(userService.byId(extractUuid(in.getActor())));
    out.verb = in.getVerb();
    out.time = in.getTime();
    LogEntry activity = logEntryService.byId(extractUuid(in.getForeignId()));
    if (activity != null) {
      Context ctx = Context.current();
      Messages messages = ctx.messages();
      out.activityId = activity.id;
      out.contentType = activity.contentType;
      if (activity.project != null)
        out.project = Project.from(activity.project);
      out.text = messages.at("activity." + activity.type + ".title",
          activity.project != null ? activity.project.name : "",
          messages.at("contentType." + activity.contentType), JsonUtils.nameOf(activity),
          FormatUtils.pretty(ctx.lang().locale(), activity.whenCreated), activity.user.name,
          activity.user.username, activity.user.id);
    }

    return out;
  }

  private static UUID extractUuid(String id) {
    String[] parts = id.split(":", 2);
    if (parts.length != 2)
      return null;

    return JsonUtils.getUuid(parts[1]);
  }

  /**
   * @param activities
   * @return
   */
  public static List<Notification> fromList(List<SimpleActivity> in) {
    // TODO: implement

    return in.stream().map(Notification::from).collect(toList());
  }
}
