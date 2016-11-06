package criterias;

import java.util.UUID;

import org.joda.time.DateTime;

import forms.ActivitySearchForm;

/**
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LogEntryCriteria extends AbstractSearchCriteria<LogEntryCriteria> {
  private UUID userIdExcluded;

  private UUID projectUserId;

  private DateTime whenCreatedMin;

  private DateTime whenCreatedMax;

  /**
   * @return the userIdExcluded
   */
  public UUID getUserIdExcluded() {
    return userIdExcluded;
  }

  /**
   * @param userIdExcluded the userIdExcluded to set
   */
  public void setUserIdExcluded(UUID userIdExcluded) {
    this.userIdExcluded = userIdExcluded;
  }

  public LogEntryCriteria withUserIdExcluded(UUID userIdExcluded) {
    setUserIdExcluded(userIdExcluded);
    return this;
  }

  public UUID getProjectUserId() {
    return projectUserId;
  }

  public void setProjectUserId(UUID projectUserId) {
    this.projectUserId = projectUserId;
  }

  public LogEntryCriteria withProjectUserId(UUID projectUserId) {
    setProjectUserId(projectUserId);
    return this;
  }

  public DateTime getWhenCreatedMin() {
    return whenCreatedMin;
  }

  public void setWhenCreatedMin(DateTime whenCreatedMin) {
    this.whenCreatedMin = whenCreatedMin;
  }

  public LogEntryCriteria withWhenCreatedMin(DateTime whenCreatedMin) {
    setWhenCreatedMin(whenCreatedMin);
    return this;
  }

  public DateTime getWhenCreatedMax() {
    return whenCreatedMax;
  }

  public void setWhenCreatedMax(DateTime whenCreatedMax) {
    this.whenCreatedMax = whenCreatedMax;
  }

  public LogEntryCriteria withWhenCreatedMax(DateTime whenCreatedMax) {
    setWhenCreatedMax(whenCreatedMax);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCacheKey() {
    return String.format("%s:%s:%s:%s:%s", super.getCacheKey(), userIdExcluded, projectUserId,
        whenCreatedMin, whenCreatedMax);
  }

  public static LogEntryCriteria from(ActivitySearchForm form) {
    return new LogEntryCriteria().with(form).withWhenCreatedMin(form.whenCreatedMin)
        .withWhenCreatedMax(form.whenCreatedMax);
  }
}
