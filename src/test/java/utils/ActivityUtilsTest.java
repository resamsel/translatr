package utils;

import com.fasterxml.jackson.databind.JsonNode;
import models.ActionType;
import models.LogEntry;
import org.junit.Test;
import play.libs.Json;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class ActivityUtilsTest {

  @Test
  public void instanceOf() {
    assertThat(new ActivityUtils(null)).isNotNull();
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
