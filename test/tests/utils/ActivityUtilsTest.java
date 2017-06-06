package tests.utils;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.Keys;
import controllers.Locales;
import models.ActionType;
import models.LogEntry;
import play.libs.Json;
import utils.ActivityUtils;

public class ActivityUtilsTest {
  @Test
  public void instanceOf() {
    assertThat(new ActivityUtils()).isNotNull();
  }

  @Test
  public void linkTo() {
    assertThat(ActivityUtils.linkTo(null)).isNull();
    assertThat(ActivityUtils.linkTo(createLogEntry(ActionType.Create, dto.User.class.getName())))
        .isNull();

    UUID uuid = UUID.randomUUID();
    long id = ThreadLocalRandom.current().nextLong();
    LogEntry activity = createLogEntry(ActionType.Create, dto.User.class.getName(), uuid);
    activity.after = Json.stringify(Json.newObject().put("username", "username"));
    assertThat(ActivityUtils.linkTo(activity)).isEqualTo(controllers.routes.Users.user("username"));
    assertThat(
        ActivityUtils.linkTo(createLogEntry(ActionType.Create, dto.Project.class.getName(), uuid)))
            .isEqualTo(controllers.routes.Projects.project(uuid));
    assertThat(
        ActivityUtils.linkTo(createLogEntry(ActionType.Create, dto.Locale.class.getName(), uuid)))
            .isEqualTo(controllers.routes.Locales.locale(uuid, Locales.DEFAULT_SEARCH,
                Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT, Locales.DEFAULT_OFFSET));
    assertThat(
        ActivityUtils.linkTo(createLogEntry(ActionType.Create, dto.Key.class.getName(), uuid)))
            .isEqualTo(controllers.routes.Keys.key(uuid, Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER,
                Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET));
    assertThat(
        ActivityUtils.linkTo(createLogEntry(ActionType.Create, dto.Message.class.getName(), uuid)))
            .isNull();
    LogEntry logEntry = createLogEntry(ActionType.Create, dto.Message.class.getName(), uuid);
    logEntry.after =
        ((ObjectNode) Json.parse(logEntry.after)).put("keyId", uuid.toString()).toString();
    assertThat(ActivityUtils.linkTo(logEntry)).isEqualTo(controllers.routes.Keys.key(uuid,
        Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET));
    assertThat(ActivityUtils
        .linkTo(createLogEntry(ActionType.Create, dto.ProjectUser.class.getName(), uuid)))
            .isEqualTo(controllers.routes.Projects.members(uuid));
    assertThat(ActivityUtils
        .linkTo(createLogEntry(ActionType.Create, dto.AccessToken.class.getName(), id)))
            .isEqualTo(controllers.routes.Profiles.accessTokenEdit(id));
    assertThat(
        ActivityUtils.linkTo(createLogEntry(ActionType.Create, dto.Suggestion.class.getName(), id)))
            .isNull();
  }

  @Test
  public void iconOf() {
    assertThat(ActivityUtils.iconOf(null)).isNull();
    assertThat(ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.User.class.getName())))
        .isEqualTo(ActivityUtils.USER_ICON);
    assertThat(ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.Project.class.getName())))
        .isEqualTo(ActivityUtils.PROJECT_ICON);
    assertThat(ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.Locale.class.getName())))
        .isEqualTo(ActivityUtils.LOCALE_ICON);
    assertThat(ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.Key.class.getName())))
        .isEqualTo(ActivityUtils.KEY_ICON);
    assertThat(ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.Message.class.getName())))
        .isEqualTo(ActivityUtils.MESSAGE_ICON);
    assertThat(
        ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.ProjectUser.class.getName())))
            .isEqualTo(ActivityUtils.PROJECT_USER_ICON);
    assertThat(
        ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.AccessToken.class.getName())))
            .isEqualTo(ActivityUtils.ACCESS_TOKEN_ICON);
    assertThat(
        ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.Suggestion.class.getName())))
            .isEqualTo("");
  }

  @Test
  public void colorOf() {
    assertThat(ActivityUtils.colorOf(null)).isNull();
    assertThat(ActivityUtils.colorOf(createLogEntry(ActionType.Create, dto.User.class.getName())))
        .isEqualTo(ActivityUtils.USER_COLOR);
    assertThat(
        ActivityUtils.colorOf(createLogEntry(ActionType.Create, dto.Project.class.getName())))
            .isEqualTo(ActivityUtils.PROJECT_COLOR);
    assertThat(ActivityUtils.colorOf(createLogEntry(ActionType.Create, dto.Locale.class.getName())))
        .isEqualTo(ActivityUtils.LOCALE_COLOR);
    assertThat(ActivityUtils.colorOf(createLogEntry(ActionType.Create, dto.Key.class.getName())))
        .isEqualTo(ActivityUtils.KEY_COLOR);
    assertThat(
        ActivityUtils.colorOf(createLogEntry(ActionType.Create, dto.Message.class.getName())))
            .isEqualTo(ActivityUtils.MESSAGE_COLOR);
    assertThat(
        ActivityUtils.colorOf(createLogEntry(ActionType.Create, dto.ProjectUser.class.getName())))
            .isEqualTo(ActivityUtils.PROJECT_USER_COLOR);
    assertThat(
        ActivityUtils.colorOf(createLogEntry(ActionType.Create, dto.AccessToken.class.getName())))
            .isEqualTo(ActivityUtils.ACCESS_TOKEN_COLOR);
    assertThat(
        ActivityUtils.colorOf(createLogEntry(ActionType.Create, dto.Suggestion.class.getName())))
            .isEqualTo("");
  }

  @Test
  public void nameOf() {
    assertThat(ActivityUtils.nameOf(null)).isNull();
    assertThat(ActivityUtils
        .nameOf(createLogEntry(ActionType.Create, dto.User.class.getName(), Json.newObject())))
            .isNull();
    assertThat(
        ActivityUtils.nameOf(createLogEntry(ActionType.Create, dto.User.class.getName(), "U")))
            .isEqualTo("U");
    assertThat(
        ActivityUtils.nameOf(createLogEntry(ActionType.Create, dto.Project.class.getName(), "P")))
            .isEqualTo("P");
    assertThat(
        ActivityUtils.nameOf(createLogEntry(ActionType.Create, dto.Locale.class.getName(), "L")))
            .isEqualTo("L");
    assertThat(
        ActivityUtils.nameOf(createLogEntry(ActionType.Create, dto.Key.class.getName(), "K")))
            .isEqualTo("K");
    assertThat(ActivityUtils.nameOf(createLogEntry(ActionType.Create, dto.Message.class.getName(),
        Json.newObject().put("keyName", "K").put("localeName", "L")))).isEqualTo("K (L)");
    assertThat(
        ActivityUtils.nameOf(createLogEntry(ActionType.Create, dto.ProjectUser.class.getName(),
            Json.newObject().put("projectName", "P").put("userName", "U")))).isEqualTo("P (U)");
    assertThat(ActivityUtils
        .nameOf(createLogEntry(ActionType.Create, dto.AccessToken.class.getName(), "A")))
            .isEqualTo("A");
    assertThat(ActivityUtils
        .nameOf(createLogEntry(ActionType.Create, dto.Suggestion.class.getName(), "A")))
            .isEqualTo("");
  }

  @Test
  public void parse() {
    assertThat(ActivityUtils
        .parse(createLogEntry(ActionType.Create, dto.User.class.getName(), Json.newObject())))
            .isEqualTo(Json.newObject());
    assertThat(ActivityUtils
        .parse(createLogEntry(ActionType.Delete, dto.User.class.getName(), Json.newObject())))
            .isEqualTo(Json.newObject());
    assertThat(ActivityUtils
        .parse(createLogEntry(ActionType.Create, dto.User.class.getName(), (JsonNode) null)))
            .isEqualTo(Json.newObject());
    assertThat(ActivityUtils
        .parse(createLogEntry(ActionType.Delete, dto.User.class.getName(), (JsonNode) null)))
            .isEqualTo(Json.newObject());
  }

  private LogEntry createLogEntry(ActionType type, String contentType) {
    return createLogEntry(type, contentType, Json.newObject());
  }

  private LogEntry createLogEntry(ActionType type, String contentType, UUID uuid) {
    return createLogEntry(type, contentType, Json.newObject().put("id", uuid.toString()));
  }

  private LogEntry createLogEntry(ActionType type, String contentType, long id) {
    return createLogEntry(type, contentType, Json.newObject().put("id", id));
  }

  private LogEntry createLogEntry(ActionType type, String contentType, String name) {
    return createLogEntry(type, contentType, Json.newObject().put("name", name));
  }

  private LogEntry createLogEntry(ActionType type, String contentType, JsonNode json) {
    LogEntry out = new LogEntry();

    out.type = type;
    out.contentType = contentType;

    switch (type) {
      case Create:
        if (json != null)
          out.after = json.toString();
        break;
      case Delete:
        if (json != null)
          out.before = json.toString();
        break;
      default:
        break;
    }

    return out;
  }
}
