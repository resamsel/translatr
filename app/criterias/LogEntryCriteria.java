package criterias;

import forms.ActivitySearchForm;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LogEntryCriteria extends AbstractSearchCriteria<LogEntryCriteria> {

  private List<UUID> ids;

  /**
   * @return the ids
   */
  public List<UUID> getIds() {
    return ids;
  }

  /**
   * @param ids the ids to set
   */
  public void setIds(List<UUID> ids) {
    this.ids = ids;
  }

  /**
   * @param ids
   * @return
   */
  public LogEntryCriteria withIds(List<UUID> ids) {
    setIds(ids);
    return this;
  }

  public static LogEntryCriteria from(ActivitySearchForm form) {
    return new LogEntryCriteria().with(form);
  }
}
