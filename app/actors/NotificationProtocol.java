package actors;

import java.util.UUID;
import models.ActionType;
import models.LogEntry;
import play.libs.Json;
import utils.ActivityUtils;

public class NotificationProtocol {

  public static class PublishNotification {

    public final UUID id;
    public final ActionType type;
    public final String name;
    public final String contentId;
    public final UUID userId;
    public final UUID projectId;

    public PublishNotification(LogEntry logEntry) {
      this.id = logEntry.id;
      this.type = logEntry.type;
      this.name = ActivityUtils.nameOf(logEntry);
      this.contentId = toContentId(logEntry);
      this.userId = logEntry.user.id;
      this.projectId = logEntry.project.id;
    }

    private static String toContentId(LogEntry logEntry) {
      String slug = logEntry.getSimpleContentType();
      String id;
      switch (logEntry.type) {
        case Create:
        case Update:
        case Login:
        case Logout:
          id = Json.parse(logEntry.after).get("id").asText();
          break;
        case Delete:
          id = Json.parse(logEntry.before).get("id").asText();
          break;
        default:
          id = "0";
          break;
      }
      return String.format("%s:%s", slug, id);
    }
  }

  public static class FollowNotification {

    public final UUID userId;
    public final UUID projectId;

    public FollowNotification(UUID userId, UUID projectId) {
      this.userId = userId;
      this.projectId = projectId;
    }
  }
}
