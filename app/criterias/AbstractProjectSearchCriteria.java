package criterias;

import play.mvc.Http;
import utils.JsonUtils;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.join;

public abstract class AbstractProjectSearchCriteria<T extends AbstractProjectSearchCriteria<T>> extends
    AbstractSearchCriteria<T> {

  private UUID projectId;
  private UUID projectOwnerId;

  AbstractProjectSearchCriteria(String type) {
    super(type);
  }

  /**
   * @return the projectId
   */
  public UUID getProjectId() {
    return projectId;
  }

  /**
   * @param projectId the projectId to set
   */
  public void setProjectId(UUID projectId) {
    this.projectId = projectId;
  }

  /**
   * @param projectId the projectId to set
   * @return this
   */
  public T withProjectId(UUID projectId) {
    setProjectId(projectId);
    return self;
  }

  /**
   * @return the projectOwnerId
   */
  public UUID getProjectOwnerId() {
    return projectOwnerId;
  }

  /**
   * @param projectOwnerId the projectOwnerId to set
   */
  public void setProjectOwnerId(UUID projectOwnerId) {
    this.projectOwnerId = projectOwnerId;
  }

  /**
   * @param projectOwnerId the projectOwnerId to set
   * @return this
   */
  public T withProjectOwnerId(UUID projectOwnerId) {
    setProjectOwnerId(projectOwnerId);
    return self;
  }

  @Override
  public T with(Http.Request request) {
    return super.with(request)
        .withProjectId(JsonUtils.getUuid(request.getQueryString("projectId")))
        .withProjectOwnerId(JsonUtils.getUuid(request.getQueryString("projectOwnerId")));
  }

  /**
   * Must be overridden by subclasses to allow caching.
   */
  public final String getCacheKey() {
    return String.format(
        "%s:criteria:%s:%s:%s:%s:%s:%d:%d:%s:%s",
        type,
        projectId != null ? projectId : "",
        projectOwnerId != null ? projectOwnerId : "",
        getUserId() != null ? getUserId() : "",
        getSearch() != null ? getSearch() : "",
        getOrder() != null ? getOrder() : "",
        getLimit(),
        getOffset(),
        getFetches() != null ? join(getFetches(), ',') : "",
        getCacheKeyParticle()
    );
  }
}
