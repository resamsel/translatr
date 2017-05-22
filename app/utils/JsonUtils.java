package utils;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.routes;
import models.LogEntry;
import play.libs.Json;
import play.mvc.Call;

/**
 *
 * @author resamsel
 * @version 2 Oct 2016
 */
public class JsonUtils {
  public static final String USER_ICON = "account_circle";
  public static final String PROJECT_ICON = "view_quilt";
  public static final String LOCALE_ICON = "book";
  public static final String KEY_ICON = "vpn_key";
  public static final String MESSAGE_ICON = "message";
  public static final String ACCESS_TOKEN_ICON = "accessibility";
  public static final String PROJECT_USER_ICON = "group_work";

  public static final String USER_COLOR = "teal";
  public static final String PROJECT_COLOR = "orange";
  public static final String LOCALE_COLOR = "blue";
  public static final String KEY_COLOR = "light-green";
  public static final String MESSAGE_COLOR = "red";
  public static final String ACCESS_TOKEN_COLOR = "green";
  public static final String PROJECT_USER_COLOR = "purple";

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
        return getAsText(node, "name");
      case "dto.Message":
        return String.format("%s (%s)", getAsText(node, "keyName"), getAsText(node, "localeName"));
      case "dto.ProjectUser":
        return String.format("%s (%s)", getAsText(node, "projectName"),
            getAsText(node, "userName"));
      default:
        return "";
    }
  }

  public static Call linkTo(LogEntry activity) {
    if (activity == null)
      return null;

    JsonNode node = parse(activity);
    Long id = getId(node);
    UUID uuid = getUuid(node);

    switch (activity.contentType) {
      case "dto.User":
        if (uuid != null)
          return routes.Users.user(uuid);
        break;
      case "dto.Project":
        if (uuid != null)
          return routes.Projects.project(uuid);
        break;
      case "dto.Locale":
        if (uuid != null)
          return routes.Locales.locale(uuid);
        break;
      case "dto.Key":
        if (uuid != null)
          return routes.Keys.key(uuid);
        break;
      case "dto.Message":
        UUID keyId = getUuid(node, "keyId");
        if (keyId != null)
          return routes.Keys.key(keyId);
        break;
      case "dto.ProjectUser":
        if (uuid != null)
          return routes.Projects.members(uuid);
        break;
      case "dto.AccessToken":
        if (id != null)
          return routes.Profiles.accessTokenEdit(id);
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

  public static Long getId(JsonNode node) {
    return getId(node, "id");
  }

  private static Long getId(JsonNode node, String key) {
    if (!node.hasNonNull(key))
      return null;

    return node.get(key).asLong();
  }

  public static UUID getUuid(JsonNode node) {
    return getUuid(node, "id");
  }

  public static UUID getUuid(JsonNode node, String key) {
    if (!node.hasNonNull(key))
      return null;

    return getUuid(node.get(key).asText());
  }

  public static UUID getUuid(String uuid) {
    if (uuid == null || uuid.trim().length() < 1)
      return null;

    try {
      return UUID.fromString(uuid);
    } catch (IllegalArgumentException e) {
      return null;
    }
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

  /**
   * @param node
   * @param string
   * @return
   */
  private static String getAsText(JsonNode node, String fieldName) {
    if (!node.hasNonNull(fieldName))
      return null;

    return node.get(fieldName).asText();
  }
}
