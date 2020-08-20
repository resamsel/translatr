package dto;

import io.ebean.PagedList;
import io.getstream.client.model.activities.AggregatedActivity;
import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.StreamResponse;
import mappers.AggregatedNotificationMapper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class NotificationsPaged extends Dto implements PagedList<AggregatedNotification> {
  private static final long serialVersionUID = 5155380848902188888L;

  private List<AggregatedNotification> list;

  public NotificationsPaged(StreamResponse<AggregatedActivity<SimpleActivity>> delegate) {
    if (delegate != null)
      this.list = AggregatedNotificationMapper.toDto(delegate.getResults());
    else
      this.list = Collections.emptyList();
  }

  /**
   * @return the list
   */
  @Override
  public List<AggregatedNotification> getList() {
    return list;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadCount() {}

  /**
   * {@inheritDoc}
   */
  @Override
  public Future<Integer> getFutureCount() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getTotalCount() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getTotalPageCount() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPageSize() {
    return 0;
  }

  @Override
  public int getPageIndex() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasPrev() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDisplayXtoYofZ(String to, String of) {
    return null;
  }
}
