package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.translatr.dto.*;
import com.translatr.model.ActionType;
import com.translatr.model.LogEntry;
import com.translatr.util.ActivityUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // -------------------------------------------------------------------------
    // contentTypeOf
    // -------------------------------------------------------------------------

    @Test
    void contentTypeOfEmptyString() {
        LogEntry activity = logEntry(ActionType.Create, "");
        assertThat(ActivityUtils.contentTypeOf(activity)).isEqualTo("");
    }

    @Test
    void contentTypeOfSimpleName() {
        LogEntry activity = logEntry(ActionType.Create, "Project");
        assertThat(ActivityUtils.contentTypeOf(activity)).isEqualTo("Project");
    }

    @Test
    void contentTypeOfQualifiedName() {
        LogEntry activity = logEntry(ActionType.Create, ProjectDto.class.getName());
        assertThat(ActivityUtils.contentTypeOf(activity)).isEqualTo("Project");
    }

    // -------------------------------------------------------------------------
    // iconOf
    // -------------------------------------------------------------------------

    @Test
    void iconOf() {
        assertThat(ActivityUtils.iconOf(null)).isNull();
        assertThat(ActivityUtils.iconOf(logEntry(ActionType.Create, UserDto.class.getName())))
                .isEqualTo(ActivityUtils.USER_ICON);
        assertThat(ActivityUtils.iconOf(logEntry(ActionType.Create, ProjectDto.class.getName())))
                .isEqualTo(ActivityUtils.PROJECT_ICON);
        assertThat(ActivityUtils.iconOf(logEntry(ActionType.Update, LocaleDto.class.getName())))
                .isEqualTo(ActivityUtils.LOCALE_ICON);
        assertThat(ActivityUtils.iconOf(logEntry(ActionType.Create, KeyDto.class.getName())))
                .isEqualTo(ActivityUtils.KEY_ICON);
        assertThat(ActivityUtils.iconOf(logEntry(ActionType.Create, MessageDto.class.getName())))
                .isEqualTo(ActivityUtils.MESSAGE_ICON);
        assertThat(ActivityUtils.iconOf(logEntry(ActionType.Create, AccessTokenDto.class.getName())))
                .isEqualTo(ActivityUtils.ACCESS_TOKEN_ICON);
        // Unknown type → empty string
        assertThat(ActivityUtils.iconOf(logEntry(ActionType.Create, "dto.Suggestion")))
                .isEqualTo("");
    }

    // -------------------------------------------------------------------------
    // colorOf
    // -------------------------------------------------------------------------

    @Test
    void colorOf() {
        assertThat(ActivityUtils.colorOf(null)).isNull();
        assertThat(ActivityUtils.colorOf(logEntry(ActionType.Create, UserDto.class.getName())))
                .isEqualTo(ActivityUtils.USER_COLOR);
        assertThat(ActivityUtils.colorOf(logEntry(ActionType.Create, ProjectDto.class.getName())))
                .isEqualTo(ActivityUtils.PROJECT_COLOR);
        assertThat(ActivityUtils.colorOf(logEntry(ActionType.Create, LocaleDto.class.getName())))
                .isEqualTo(ActivityUtils.LOCALE_COLOR);
        assertThat(ActivityUtils.colorOf(logEntry(ActionType.Create, KeyDto.class.getName())))
                .isEqualTo(ActivityUtils.KEY_COLOR);
        assertThat(ActivityUtils.colorOf(logEntry(ActionType.Create, MessageDto.class.getName())))
                .isEqualTo(ActivityUtils.MESSAGE_COLOR);
        assertThat(ActivityUtils.colorOf(logEntry(ActionType.Create, AccessTokenDto.class.getName())))
                .isEqualTo(ActivityUtils.ACCESS_TOKEN_COLOR);
        assertThat(ActivityUtils.colorOf(logEntry(ActionType.Create, "dto.Suggestion")))
                .isEqualTo("");
    }

    // -------------------------------------------------------------------------
    // nameOf
    // -------------------------------------------------------------------------

    @Test
    void nameOf() {
        assertThat(ActivityUtils.nameOf(null)).isNull();

        // Null JSON → null name
        assertThat(ActivityUtils.nameOf(logEntry(ActionType.Create, UserDto.class.getName(), MAPPER.createObjectNode())))
                .isNull();

        // Simple name field
        assertThat(ActivityUtils.nameOf(logEntry(ActionType.Create, UserDto.class.getName(), "U")))
                .isEqualTo("U");
        assertThat(ActivityUtils.nameOf(logEntry(ActionType.Create, ProjectDto.class.getName(), "P")))
                .isEqualTo("P");
        assertThat(ActivityUtils.nameOf(logEntry(ActionType.Create, LocaleDto.class.getName(), "L")))
                .isEqualTo("L");
        assertThat(ActivityUtils.nameOf(logEntry(ActionType.Create, KeyDto.class.getName(), "K")))
                .isEqualTo("K");

        // Message: "keyName (localeName)"
        ObjectNode msgNode = MAPPER.createObjectNode().put("keyName", "K").put("localeName", "L");
        assertThat(ActivityUtils.nameOf(logEntry(ActionType.Create, MessageDto.class.getName(), msgNode)))
                .isEqualTo("K (L)");

        // AccessToken: simple name
        assertThat(ActivityUtils.nameOf(logEntry(ActionType.Create, AccessTokenDto.class.getName(), "A")))
                .isEqualTo("A");

        // Unknown type → empty string
        assertThat(ActivityUtils.nameOf(logEntry(ActionType.Create, "dto.Suggestion", "A")))
                .isEqualTo("");
    }

    // -------------------------------------------------------------------------
    // parse
    // -------------------------------------------------------------------------

    @Test
    void parse() {
        ObjectNode empty = MAPPER.createObjectNode();

        // Create: uses "after" field
        assertThat(ActivityUtils.parse(logEntry(ActionType.Create, UserDto.class.getName(), empty)))
                .isEqualTo(empty);

        // Delete: uses "before" field
        assertThat(ActivityUtils.parse(logEntry(ActionType.Delete, UserDto.class.getName(), empty)))
                .isEqualTo(empty);

        // Null JSON: falls back to empty object
        assertThat(ActivityUtils.parse(logEntry(ActionType.Create, UserDto.class.getName(), (JsonNode) null)))
                .isEqualTo(empty);
        assertThat(ActivityUtils.parse(logEntry(ActionType.Delete, UserDto.class.getName(), (JsonNode) null)))
                .isEqualTo(empty);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static LogEntry logEntry(ActionType type, String contentType) {
        return logEntry(type, contentType, MAPPER.createObjectNode());
    }

    private static LogEntry logEntry(ActionType type, String contentType, String name) {
        return logEntry(type, contentType, MAPPER.createObjectNode().put("name", name));
    }

    private static LogEntry logEntry(ActionType type, String contentType, JsonNode json) {
        LogEntry e = new LogEntry();
        e.type        = type;
        e.contentType = contentType;
        String s = json != null ? json.toString() : null;
        switch (type) {
            case Create, Update, Login, Logout -> e.after  = s;
            case Delete                        -> e.before = s;
        }
        return e;
    }
}
