package utils;

import com.fasterxml.jackson.databind.JsonNode;
import models.LogEntry;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 * @author resamsel
 * @version 2 Oct 2016
 */
@Singleton
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
  private final MessagesApi messagesApi;

  @Inject
  public ActivityUtils(MessagesApi messagesApi) {
    this.messagesApi = messagesApi;
  }

  public static String nameOf(LogEntry activity) {
    if (activity == null)
      return null;

    JsonNode node = parse(activity);

    switch (contentTypeOf(activity)) {
      case "User":
      case "Project":
      case "Locale":
      case "Key":
      case "AccessToken":
        return JsonUtils.getAsText(node, "name");
      case "Message":
        return String.format("%s (%s)", JsonUtils.getAsText(node, "keyName"),
            JsonUtils.getAsText(node, "localeName"));
      case "ProjectUser":
        return String.format("%s (%s)", JsonUtils.getAsText(node, "projectName"),
            JsonUtils.getAsText(node, "userName"));
      default:
        return "";
    }
  }

  public static String iconOf(LogEntry activity) {
    if (activity == null)
      return null;

    switch (contentTypeOf(activity)) {
      case "Project":
        return PROJECT_ICON;
      case "Locale":
        return LOCALE_ICON;
      case "Key":
        return KEY_ICON;
      case "AccessToken":
        return ACCESS_TOKEN_ICON;
      case "Message":
        return MESSAGE_ICON;
      case "User":
        return USER_ICON;
      case "ProjectUser":
        return PROJECT_USER_ICON;
      default:
        return "";
    }
  }

  public static String colorOf(LogEntry activity) {
    if (activity == null)
      return null;

    switch (contentTypeOf(activity)) {
      case "Project":
        return PROJECT_COLOR;
      case "Locale":
        return LOCALE_COLOR;
      case "Key":
        return KEY_COLOR;
      case "Message":
        return MESSAGE_COLOR;
      case "User":
        return USER_COLOR;
      case "AccessToken":
        return ACCESS_TOKEN_COLOR;
      case "ProjectUser":
        return PROJECT_USER_COLOR;
      default:
        return "";
    }
  }

  static String contentTypeOf(LogEntry activity) {
    String contentType = activity.contentType;

    return contentType.substring(contentType.lastIndexOf(".") + 1);
  }

  public String titleOf(LogEntry logEntry, Http.Request request) {
    Messages messages = messagesApi.preferred(request);

    return messages.at("activity." + logEntry.type.normalize() + ".title",
        logEntry.project != null ? logEntry.project.name : "",
        messages.at("contentType." + logEntry.contentType), ActivityUtils.nameOf(logEntry),
        FormatUtils.pretty(messages.lang().locale(), logEntry.whenCreated), logEntry.user.name,
        logEntry.user.username, logEntry.user.id);
  }

  public  String infoOf(LogEntry logEntry, Http.Request request) {
    Messages messages = messagesApi.preferred(request);

    return messages.at("activity." + logEntry.type.normalize() + ".info",
        messages.at("contentType." + logEntry.contentType),
        FormatUtils.pretty(messages.lang().locale(), logEntry.whenCreated));
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
