package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Keys;
import controllers.Locales;
import controllers.Projects;
import models.ActionType;
import models.LogEntry;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Call;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SuppressWarnings("ConstantConditions")
public class ActivityUtilsTest {

  @Test
  public void instanceOf() {
    assertThat(new ActivityUtils()).isNotNull();
  }

  @Test
  public void linkToWithNull() {
    // given
    LogEntry activity = null;

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual).isNull();
  }

  @Test
  public void linkToWithEmptyPayload() {
    // given
    LogEntry activity = createLogEntry(ActionType.Create, dto.User.class.getName());

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual).isNull();
  }

  @Test
  public void linkToWithUserActivity() {
    // given
    UUID uuid = UUID.randomUUID();
    LogEntry activity = createLogEntry(ActionType.Create, dto.User.class.getName(), uuid);
    activity.after = Json.stringify(Json.newObject().put("username", "username"));

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual).isEqualTo(controllers.routes.Users.user("username"));
  }

  @Test
  public void linkToWithProject() {
    // given
    String name = "name";
    String ownerUsername = "username";
    UUID ownerId = UUID.randomUUID();
    ObjectNode project = Json.newObject()
        .put("name", name)
        .put("ownerId", ownerId.toString())
        .put("ownerUsername", ownerUsername);
    LogEntry activity = createLogEntry(ActionType.Create, dto.Project.class.getName(), project);

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual).isEqualTo(controllers.routes.Projects.projectBy(ownerUsername, name));
  }

  @Test
  public void linkToWithLocale() {
    // given
    String ownerUsername = "username";
    String name = "name";
    String localeName = "en";
    ObjectNode locale = Json.newObject()
        .put("name", localeName)
        .put("pathName", localeName)
        .put("projectName", name)
        .put("projectOwnerUsername", ownerUsername);
    LogEntry activity = createLogEntry(ActionType.Create, dto.Locale.class.getName(), locale);

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual)
        .isEqualTo(controllers.routes.Locales.localeBy(ownerUsername, name, localeName,
            Locales.DEFAULT_SEARCH, Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT,
            Locales.DEFAULT_OFFSET));
  }

  @Test
  public void linkToWithKey() {
    // given
    String ownerUsername = "username";
    String name = "name";
    String keyName = "a/b/c";
    String keyPathName = "a%2Fb%2Fc";
    ObjectNode key = Json.newObject()
        .put("name", keyName)
        .put("pathName", keyPathName)
        .put("projectName", name)
        .put("projectOwnerUsername", ownerUsername);
    LogEntry activity = createLogEntry(ActionType.Create, dto.Key.class.getName(), key);

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual)
        .isEqualTo(controllers.routes.Keys.keyBy(ownerUsername, name, keyName,
            Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET));
  }

  @Test
  public void linkToWithMessageWithEmptyPayload() {
    // given
    UUID uuid = UUID.randomUUID();
    LogEntry activity = createLogEntry(ActionType.Update, dto.Message.class.getName(), uuid);

    // when
    Throwable thrown = catchThrowable(() -> ActivityUtils.linkTo(activity));

    // then
    assertThat(thrown)
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Key project is null")
        .hasCause(null);
  }

  @Test
  public void linkToWithMessage() {
    // given
    UUID projectId = UUID.randomUUID();
    String ownerUsername = "username";
    String name = "name";
    String localeName = "en";
    String keyName = "a/b/c";
    String keyPathName = "a%2Fb%2Fc";
    ObjectNode message = Json.newObject()
        .put("value", "abc")
        .put("projectId", projectId.toString())
        .put("projectName", name)
        .put("projectOwnerUsername", ownerUsername)
        .put("localeName", localeName)
        .put("keyName", keyName)
        .put("keyPathName", keyPathName);
    LogEntry activity = createLogEntry(ActionType.Create, dto.Message.class.getName(), message);
    activity.after = message.toString();

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual)
        .isEqualTo(controllers.routes.Keys.keyBy(ownerUsername, name, keyPathName,
            Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET));
  }

  @Test
  public void linkToWithProjectUser() {
    // given
    String name = "name";
    String ownerUsername = "username";
    ObjectNode projectUser = Json.newObject()
        .put("projectName", name)
        .put("projectOwnerUsername", ownerUsername);
    LogEntry activity = createLogEntry(ActionType.Create, dto.ProjectUser.class.getName(), projectUser);

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual)
        .isEqualTo(
            controllers.routes.Projects.membersBy(ownerUsername, name, Projects.DEFAULT_SEARCH,
                Projects.DEFAULT_ORDER, Projects.DEFAULT_LIMIT, Projects.DEFAULT_OFFSET));
  }

  @Test
  public void linkToWithAccessToken() {
    // given
    long id = ThreadLocalRandom.current().nextLong();
    String ownerUsername = "username";
    LogEntry activity = createLogEntry(ActionType.Create, dto.AccessToken.class.getName(),
        Json.newObject().put("id", id).put("username", "username"));

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual).isEqualTo(controllers.routes.Users.accessTokenEdit(ownerUsername, id));
  }

  @Test
  public void linkToWithSuggestion() {
    // given
    long id = ThreadLocalRandom.current().nextLong();
    LogEntry activity = createLogEntry(ActionType.Create, dto.Suggestion.class.getName(), id);

    // when
    Call actual = ActivityUtils.linkTo(activity);

    // then
    assertThat(actual).isNull();
  }

  @Test
  public void contentTypeOfEmptyString() {
    // given
    LogEntry activity = createLogEntry(ActionType.Create, "", "");

    // when
    String actual = ActivityUtils.contentTypeOf(activity);

    // then
    assertThat(actual).isEqualTo("");
  }

  @Test
  public void contentTypeOfSimpleName() {
    // given
    LogEntry activity = createLogEntry(ActionType.Create, "Project", "");

    // when
    String actual = ActivityUtils.contentTypeOf(activity);

    // then
    assertThat(actual).isEqualTo("Project");
  }

  @Test
  public void contentTypeOfQualifiedName() {
    // given
    LogEntry activity = createLogEntry(ActionType.Create, "dto.Project", "");

    // when
    String actual = ActivityUtils.contentTypeOf(activity);

    // then
    assertThat(actual).isEqualTo("Project");
  }

  @Test
  public void iconOf() {
    assertThat(ActivityUtils.iconOf(null)).isNull();
    assertThat(ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.User.class.getName())))
        .isEqualTo(ActivityUtils.USER_ICON);
    assertThat(ActivityUtils.iconOf(createLogEntry(ActionType.Create, dto.Project.class.getName())))
        .isEqualTo(ActivityUtils.PROJECT_ICON);
    assertThat(ActivityUtils.iconOf(createLogEntry(ActionType.Update, dto.Locale.class.getName())))
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

  @SuppressWarnings("SameParameterValue")
  private LogEntry createLogEntry(ActionType type, String contentType, long id) {
    return createLogEntry(type, contentType, Json.newObject().put("id", id));
  }

  @SuppressWarnings("SameParameterValue")
  private LogEntry createLogEntry(ActionType type, String contentType, String name) {
    return createLogEntry(type, contentType, Json.newObject().put("name", name));
  }

  private LogEntry createLogEntry(ActionType type, String contentType, JsonNode json) {
    LogEntry out = new LogEntry();

    out.type = type;
    out.contentType = contentType;

    switch (type) {
      case Create:
        if (json != null) {
          out.after = json.toString();
        }
        break;
      case Delete:
        if (json != null) {
          out.before = json.toString();
        }
        break;
      default:
        break;
    }

    return out;
  }
}
