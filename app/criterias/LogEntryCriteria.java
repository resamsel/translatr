package criterias;

import forms.ActivitySearchForm;
import play.mvc.Http;

import java.util.List;
import java.util.Optional;
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

  @Override
  public LogEntryCriteria with(Http.Request request) {
    return super
        .with(request)
        .withUserId(Optional.ofNullable(request.getQueryString("userId"))
            .map(UUID::fromString)
            .orElse(null))
        .withProjectId(Optional.ofNullable(request.getQueryString("projectId"))
            .map(UUID::fromString)
            .orElse(null));
  }

  public static LogEntryCriteria from(ActivitySearchForm form) {
    return new LogEntryCriteria().with(form);
  }

  public static LogEntryCriteria from(Http.Request request) {
    return new LogEntryCriteria().with(request);
  }

  @Override
  protected String getCacheKeyParticle() {
    return String.format("%s", ids);
  }
}
