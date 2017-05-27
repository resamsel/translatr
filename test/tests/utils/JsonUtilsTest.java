package tests.utils;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.Keys;
import models.ActionType;
import models.LogEntry;
import play.libs.Json;
import utils.JsonUtils;

public class JsonUtilsTest {
  @Test
  public void instanceOf() {
    assertThat(new JsonUtils()).isNotNull();
  }

  @Test
  public void linkTo() {
    assertThat(JsonUtils.linkTo(null)).isNull();
    assertThat(JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.User.class.getName())))
        .isNull();

    UUID uuid = UUID.randomUUID();
    long id = ThreadLocalRandom.current().nextLong();
    assertThat(JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.User.class.getName(), uuid)))
        .isEqualTo(controllers.routes.Users.user(uuid));
    assertThat(
        JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.Project.class.getName(), uuid)))
            .isEqualTo(controllers.routes.Projects.project(uuid));
    assertThat(
        JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.Locale.class.getName(), uuid)))
            .isEqualTo(controllers.routes.Locales.locale(uuid));
    assertThat(JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.Key.class.getName(), uuid)))
        .isEqualTo(controllers.routes.Keys.key(uuid, Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER,
            Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET));
    assertThat(
        JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.Message.class.getName(), uuid)))
            .isNull();
    LogEntry logEntry = createLogEntry(ActionType.Create, dto.Message.class.getName(), uuid);
    logEntry.after =
        ((ObjectNode) Json.parse(logEntry.after)).put("keyId", uuid.toString()).toString();
    assertThat(JsonUtils.linkTo(logEntry)).isEqualTo(controllers.routes.Keys.key(uuid,
        Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET));
    assertThat(
        JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.ProjectUser.class.getName(), uuid)))
            .isEqualTo(controllers.routes.Projects.members(uuid));
    assertThat(
        JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.AccessToken.class.getName(), id)))
            .isEqualTo(controllers.routes.Profiles.accessTokenEdit(id));
    assertThat(
        JsonUtils.linkTo(createLogEntry(ActionType.Create, dto.Suggestion.class.getName(), id)))
            .isNull();
  }

  @Test
  public void iconOf() {
    assertThat(JsonUtils.iconOf(null)).isNull();
    assertThat(JsonUtils.iconOf(createLogEntry(ActionType.Create, dto.User.class.getName())))
        .isEqualTo(JsonUtils.USER_ICON);
    assertThat(JsonUtils.iconOf(createLogEntry(ActionType.Create, dto.Project.class.getName())))
        .isEqualTo(JsonUtils.PROJECT_ICON);
    assertThat(JsonUtils.iconOf(createLogEntry(ActionType.Create, dto.Locale.class.getName())))
        .isEqualTo(JsonUtils.LOCALE_ICON);
    assertThat(JsonUtils.iconOf(createLogEntry(ActionType.Create, dto.Key.class.getName())))
        .isEqualTo(JsonUtils.KEY_ICON);
    assertThat(JsonUtils.iconOf(createLogEntry(ActionType.Create, dto.Message.class.getName())))
        .isEqualTo(JsonUtils.MESSAGE_ICON);
    assertThat(JsonUtils.iconOf(createLogEntry(ActionType.Create, dto.ProjectUser.class.getName())))
        .isEqualTo(JsonUtils.PROJECT_USER_ICON);
    assertThat(JsonUtils.iconOf(createLogEntry(ActionType.Create, dto.AccessToken.class.getName())))
        .isEqualTo(JsonUtils.ACCESS_TOKEN_ICON);
    assertThat(JsonUtils.iconOf(createLogEntry(ActionType.Create, dto.Suggestion.class.getName())))
        .isEqualTo("");
  }

  @Test
  public void colorOf() {
    assertThat(JsonUtils.colorOf(null)).isNull();
    assertThat(JsonUtils.colorOf(createLogEntry(ActionType.Create, dto.User.class.getName())))
        .isEqualTo(JsonUtils.USER_COLOR);
    assertThat(JsonUtils.colorOf(createLogEntry(ActionType.Create, dto.Project.class.getName())))
        .isEqualTo(JsonUtils.PROJECT_COLOR);
    assertThat(JsonUtils.colorOf(createLogEntry(ActionType.Create, dto.Locale.class.getName())))
        .isEqualTo(JsonUtils.LOCALE_COLOR);
    assertThat(JsonUtils.colorOf(createLogEntry(ActionType.Create, dto.Key.class.getName())))
        .isEqualTo(JsonUtils.KEY_COLOR);
    assertThat(JsonUtils.colorOf(createLogEntry(ActionType.Create, dto.Message.class.getName())))
        .isEqualTo(JsonUtils.MESSAGE_COLOR);
    assertThat(
        JsonUtils.colorOf(createLogEntry(ActionType.Create, dto.ProjectUser.class.getName())))
            .isEqualTo(JsonUtils.PROJECT_USER_COLOR);
    assertThat(
        JsonUtils.colorOf(createLogEntry(ActionType.Create, dto.AccessToken.class.getName())))
            .isEqualTo(JsonUtils.ACCESS_TOKEN_COLOR);
    assertThat(JsonUtils.colorOf(createLogEntry(ActionType.Create, dto.Suggestion.class.getName())))
        .isEqualTo("");
  }

  @Test
  public void nameOf() {
    assertThat(JsonUtils.nameOf(null)).isNull();
    assertThat(JsonUtils
        .nameOf(createLogEntry(ActionType.Create, dto.User.class.getName(), Json.newObject())))
            .isNull();
    assertThat(JsonUtils.nameOf(createLogEntry(ActionType.Create, dto.User.class.getName(), "U")))
        .isEqualTo("U");
    assertThat(
        JsonUtils.nameOf(createLogEntry(ActionType.Create, dto.Project.class.getName(), "P")))
            .isEqualTo("P");
    assertThat(JsonUtils.nameOf(createLogEntry(ActionType.Create, dto.Locale.class.getName(), "L")))
        .isEqualTo("L");
    assertThat(JsonUtils.nameOf(createLogEntry(ActionType.Create, dto.Key.class.getName(), "K")))
        .isEqualTo("K");
    assertThat(JsonUtils.nameOf(createLogEntry(ActionType.Create, dto.Message.class.getName(),
        Json.newObject().put("keyName", "K").put("localeName", "L")))).isEqualTo("K (L)");
    assertThat(JsonUtils.nameOf(createLogEntry(ActionType.Create, dto.ProjectUser.class.getName(),
        Json.newObject().put("projectName", "P").put("userName", "U")))).isEqualTo("P (U)");
    assertThat(
        JsonUtils.nameOf(createLogEntry(ActionType.Create, dto.AccessToken.class.getName(), "A")))
            .isEqualTo("A");
    assertThat(
        JsonUtils.nameOf(createLogEntry(ActionType.Create, dto.Suggestion.class.getName(), "A")))
            .isEqualTo("");
  }

  @Test
  public void getUuid() {
    assertThat(JsonUtils.getUuid((String) null)).isNull();
    assertThat(JsonUtils.getUuid("")).isNull();
    assertThat(JsonUtils.getUuid("   ")).isNull();
    assertThat(JsonUtils.getUuid("123456789")).isNull();
    UUID random = UUID.randomUUID();
    assertThat(JsonUtils.getUuid(random.toString())).isEqualTo(random);
  }

  @Test
  public void parse() {
    assertThat(JsonUtils
        .parse(createLogEntry(ActionType.Create, dto.User.class.getName(), Json.newObject())))
            .isEqualTo(Json.newObject());
    assertThat(JsonUtils
        .parse(createLogEntry(ActionType.Delete, dto.User.class.getName(), Json.newObject())))
            .isEqualTo(Json.newObject());
    assertThat(JsonUtils
        .parse(createLogEntry(ActionType.Create, dto.User.class.getName(), (JsonNode) null)))
            .isEqualTo(Json.newObject());
    assertThat(JsonUtils
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
