package assertions;

import models.LogEntry;
import models.Project;
import models.User;

public class LogEntryAssert extends AbstractGenericAssert<LogEntryAssert, LogEntry> {

  private LogEntryAssert(LogEntry actual) {
    super("logEntry", actual, LogEntryAssert.class);
  }

  public static LogEntryAssert assertThat(LogEntry actual) {
    return new LogEntryAssert(actual);
  }

  public LogEntryAssert userIsEqualTo(User expected) {
    return isEqualTo("user", expected, actual.user);
  }

  public LogEntryAssert projectIsEqualTo(Project expected) {
    return isEqualTo("project", expected, actual.project);
  }
}
