package dto;

import com.avaje.ebean.PagedList;
import models.LogEntry;

import java.util.function.Function;

public class ActivitiesPaged extends DtoPagedList<LogEntry, Activity> {
  /**
   * @param delegate
   * @param mapper
   */
  public ActivitiesPaged(PagedList<LogEntry> delegate, Function<LogEntry, Activity> mapper) {
    super(delegate, mapper);
  }
}
