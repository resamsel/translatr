package criterias;

import forms.ActivitySearchForm;
import java.util.List;
import java.util.UUID;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LogEntryCriteria extends AbstractProjectSearchCriteria<LogEntryCriteria> {

  private List<UUID> ids;

  public LogEntryCriteria() {
    super("activity");
  }

  /**
   * @return the ids
   */
  public List<UUID> getIds() {
    return ids;
  }

  /**
   * @param ids the ids to set
   */
  private void setIds(List<UUID> ids) {
    this.ids = ids;
  }

  public LogEntryCriteria withIds(List<UUID> ids) {
    setIds(ids);
    return this;
  }

  public static LogEntryCriteria from(ActivitySearchForm form) {
    return new LogEntryCriteria().with(form);
  }

  @Override
  protected String getCacheKeyParticle() {
    return String.format("%s", ids);
  }
}
