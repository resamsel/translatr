package criterias;

import forms.ActivitySearchForm;
import models.ActionType;
import play.mvc.Http;
import utils.JsonUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static controllers.ActivitiesApi.PARAM_TYPES;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LogEntryCriteria extends AbstractProjectSearchCriteria<LogEntryCriteria> {

  private List<UUID> ids;
  private UUID projectMemberId;
  private final List<ActionType> types = new ArrayList<>();

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

  public UUID getProjectMemberId() {
    return projectMemberId;
  }

  private void setProjectMemberId(UUID projectMemberId) {
    this.projectMemberId = projectMemberId;
  }

  public LogEntryCriteria withProjectMemberId(UUID projectMemberId) {
    setProjectMemberId(projectMemberId);
    return this;
  }

  public List<ActionType> getTypes() {
    return types;
  }

  public LogEntryCriteria withTypes(List<ActionType> types) {
    this.types.addAll(types);
    return this;
  }

  @Override
  public LogEntryCriteria with(@Nonnull Http.Request request) {
    return super
        .with(request)
        .withUserId(JsonUtils.getUuid(request.getQueryString("userId")))
        .withProjectMemberId(JsonUtils.getUuid(request.getQueryString("projectMemberId")))
        .withTypes(ActionType.fromQueryParam(request.getQueryString(PARAM_TYPES)));
  }

  public static LogEntryCriteria from(ActivitySearchForm form) {
    return new LogEntryCriteria().with(form);
  }

  public static LogEntryCriteria from(Http.Request request) {
    return new LogEntryCriteria().with(request);
  }

  @Override
  protected String getCacheKeyParticle() {
    return String.format("%s:%s", ids, projectMemberId);
  }
}
