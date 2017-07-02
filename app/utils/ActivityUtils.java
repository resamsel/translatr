package utils;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import models.LogEntry;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Http.Context;

/**
 *
 * @author resamsel
 * @version 2 Oct 2016
 */
public class ActivityUtils {
  public static final String PROJECTS_ICON = "dashboard";
  public static final String PROJECT_ICON = "view_quilt";
  public static final String USERS_ICON = "group";
  public static final String USER_ICON = "account_circle";
  public static final String LOCALE_ICON = "language";
  public static final String KEY_ICON = "vpn_key";
  public static final String MESSAGE_ICON = "message";
  public static final String WORD_ICON = "short_text";
  public static final String LINKED_ACCOUNT_ICON = "account_box";
  public static final String ACCESS_TOKEN_ICON = "vpn_key";
  public static final String PROJECT_USER_ICON = "group";
  public static final String ACTIVITY_ICON = "change_history";

  public static final String USER_COLOR = "teal";
  public static final String PROJECT_COLOR = "orange";
  public static final String LOCALE_COLOR = "blue";
  public static final String KEY_COLOR = "light-green";
  public static final String MESSAGE_COLOR = "red";
  public static final String WORD_COLOR = "green";
  public static final String ACCESS_TOKEN_COLOR = "red";
  public static final String PROJECT_USER_COLOR = "purple";
  public static final String ACTIVITY_COLOR = "green";

  public static String nameOf(LogEntry activity) {
    if (activity == null)
      return null;

    JsonNode node = parse(activity);

    switch (activity.contentType) {
      case "dto.User":
      case "dto.Project":
      case "dto.Locale":
      case "dto.Key":
      case "dto.AccessToken":
        return JsonUtils.getAsText(node, "name");
      case "dto.Message":
        return String.format("%s (%s)", JsonUtils.getAsText(node, "keyName"),
            JsonUtils.getAsText(node, "localeName"));
      case "dto.ProjectUser":
        return String.format("%s (%s)", JsonUtils.getAsText(node, "projectName"),
            JsonUtils.getAsText(node, "userName"));
      default:
        return "";
    }
  }

  public static Call linkTo(LogEntry activity) {
    if (activity == null)
      return null;

    JsonNode node = parse(activity);
    Long id = JsonUtils.getId(node);
    UUID uuid = JsonUtils.getUuid(node);

    switch (activity.contentType) {
      case "dto.User": {
        String name = JsonUtils.getAsText(node, "username");
        if (name != null)
          return controllers.routes.Users.user(JsonUtils.getAsText(node, "username"));
        break;
      }
      case "dto.Project": {
        dto.Project project = Json.fromJson(node, dto.Project.class);
        if (project != null)
          return project.route();
        break;
      }
      case "dto.Locale": {
        dto.Locale locale = Json.fromJson(node, dto.Locale.class);
        if (locale != null)
          return locale.route();
        break;
      }
      case "dto.Key": {
        dto.Key key = Json.fromJson(node, dto.Key.class);
        if (key != null)
          return key.route();
        break;
      }
      case "dto.Message": {
        dto.Message message = Json.fromJson(node, dto.Message.class);
        if (message != null)
          return message.route();
        break;
      }
      case "dto.ProjectUser":
        if (uuid != null)
          return controllers.routes.Projects.members(uuid);
        break;
      case "dto.AccessToken":
        if (id != null)
          return controllers.routes.Profiles.accessTokenEdit(id);
        break;
      default:
        break;
    }

    return null;
  }

  public static String iconOf(LogEntry activity) {
    if (activity == null)
      return null;

    switch (activity.contentType) {
      case "dto.Project":
        return PROJECT_ICON;
      case "dto.Locale":
        return LOCALE_ICON;
      case "dto.Key":
        return KEY_ICON;
      case "dto.AccessToken":
        return ACCESS_TOKEN_ICON;
      case "dto.Message":
        return MESSAGE_ICON;
      case "dto.User":
        return USER_ICON;
      case "dto.ProjectUser":
        return PROJECT_USER_ICON;
      default:
        return "";
    }
  }

  public static String colorOf(LogEntry activity) {
    if (activity == null)
      return null;

    switch (activity.contentType) {
      case "dto.Project":
        return PROJECT_COLOR;
      case "dto.Locale":
        return LOCALE_COLOR;
      case "dto.Key":
        return KEY_COLOR;
      case "dto.Message":
        return MESSAGE_COLOR;
      case "dto.User":
        return USER_COLOR;
      case "dto.AccessToken":
        return ACCESS_TOKEN_COLOR;
      case "dto.ProjectUser":
        return PROJECT_USER_COLOR;
      default:
        return "";
    }
  }

  public static String titleOf(LogEntry logEntry) {
    Context ctx = Context.current();
    Messages messages = ctx.messages();

    return messages.at("activity." + logEntry.type.normalize() + ".title",
        logEntry.project != null ? logEntry.project.name : "",
        messages.at("contentType." + logEntry.contentType), ActivityUtils.nameOf(logEntry),
        FormatUtils.pretty(ctx.lang().locale(), logEntry.whenCreated), logEntry.user.name,
        logEntry.user.username, logEntry.user.id);
  }

  public static String infoOf(LogEntry logEntry) {
    Context ctx = Context.current();
    Messages messages = ctx.messages();

    return messages.at("activity." + logEntry.type.normalize() + ".info",
        messages.at("contentType." + logEntry.contentType),
        FormatUtils.pretty(ctx.lang().locale(), logEntry.whenCreated));
  }

  public static JsonNode parse(LogEntry activity) {
    switch (activity.type) {
      case Create:
      case Update:
        if (activity.after != null)
          return Json.parse(activity.after);
        break;
      case Delete:
        if (activity.before != null)
          return Json.parse(activity.before);
        break;
      case Login:
      case Logout:
        if (activity.after != null)
          return Json.parse(activity.after);
        break;
    }

    return Json.newObject();
  }
}
