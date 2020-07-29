package utils;

import java.util.UUID;
import models.LogEntry;
import models.Project;
import models.User;

public class LogEntryRepositoryMock {

  public static LogEntry createLogEntry(UUID id, User user, Project project) {
    LogEntry t = new LogEntry();

    t.id = id;
    t.user = user;
    t.project = project;

    return t;
  }
}
