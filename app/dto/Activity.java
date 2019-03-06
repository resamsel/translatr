package dto;

import models.LogEntry;
import org.joda.time.DateTime;

import java.util.UUID;

public class Activity extends Dto {
  public final UUID id;
  public ActionType type;
  public String contentType;
  public DateTime whenCreated;
  public UUID userId;
  public String userName;
  public String userUsername;
  public String userEmail;
  public UUID projectId;
  public String projectName;
  public String before;
  public String after;

  public Activity(LogEntry in) {
    this.id = in.id;
    this.type = ActionType.valueOf(in.type.name());
    this.contentType = in.contentType;
    this.whenCreated = in.whenCreated;
    if (in.user != null) {
      this.userId = in.user.id;
      this.userName = in.user.name;
      this.userUsername = in.user.username;
      this.userEmail = in.user.email;
    }
    if (in.project != null) {
      this.projectId = in.project.id;
      this.projectName = in.project.name;
    }
    this.before = in.before;
    this.after = in.after;
  }

  public static Activity from(LogEntry in) {
    return new Activity(in);
  }

  public LogEntry toModel(LogEntry in) {
    LogEntry out = in != null ? in : new LogEntry();

    out.id = id;
    // TODO

    return out;
  }
}
