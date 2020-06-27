package mappers;

import dto.ActionType;
import dto.Activity;
import models.LogEntry;
import utils.EmailUtils;

public class ActivityMapper {
  public static Activity toDto(LogEntry in) {
    Activity out = new Activity();

    out.id = in.id;
    out.type = ActionType.valueOf(in.type.name());
    out.contentType = in.contentType;
    out.whenCreated = in.whenCreated;
    if (in.user != null) {
      out.userId = in.user.id;
      out.userName = in.user.name;
      out.userUsername = in.user.username;
      out.userEmailHash = EmailUtils.hashEmail(in.user.email);
    }
    if (in.project != null) {
      out.projectId = in.project.id;
      out.projectName = in.project.name;
    }
    out.before = in.before;
    out.after = in.after;

    return out;
  }

  public static LogEntry toModel(Activity in, LogEntry out) {
    out = out != null ? out : new LogEntry();

    out.id = in.id;
    // TODO

    return out;
  }
}
