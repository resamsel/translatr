package criterias;

import java.util.UUID;

public abstract class AbstractProjectSearchCriteria<T extends AbstractProjectSearchCriteria<T>> extends
    AbstractSearchCriteria<T> {

  private UUID projectId;

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
   * Must be overridden by subclasses to allow caching.
   */
  public String getCacheKey() {
    return String.format("%s:criteria:%s:%s:%s:%s:%d:%d", type, projectId, getUserId(), getSearch(),
        getOrder(), getLimit(), getOffset());
  }
}
