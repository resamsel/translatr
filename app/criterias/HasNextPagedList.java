package criterias;

import io.ebean.PagedList;
import io.ebean.Query;
import io.ebeaninternal.api.Monitor;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * @author resamsel
 * @version 24 Jan 2017
 */
public class HasNextPagedList<T> implements PagedList<T>, Serializable {

  private static final long serialVersionUID = 4751634978699094645L;

  @Transient
  private transient final Query<T> query;
  private final int maxRows;
  private final int firstRow;

  private final Monitor monitor = new Monitor();

  private List<T> list;
  private boolean hasNext;

  HasNextPagedList(Query<T> query) {
    this.query = query;
    this.maxRows = query.getMaxRows();
    this.firstRow = query.getFirstRow();
  }

  HasNextPagedList(List<T> list) {
    this.list = Objects.requireNonNull(list, "List is null");
    this.query = null;
    this.maxRows = list.size();
    this.firstRow = 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadCount() {
  }

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
  public List<T> getList() {
    synchronized (monitor) {
      if (list == null) {
        if (query != null) {
          if (maxRows > 0) {
            query.setMaxRows(maxRows + 1);
          }

          list = query.findList();

          if (maxRows > 0) {
            query.setMaxRows(maxRows);
          }

          if (hasNext = maxRows > 0 && list.size() > maxRows) {
            list.subList(maxRows, list.size()).clear();
          }
        } else {
          list = Collections.emptyList();
        }
      }

      return list;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getTotalCount() {
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getTotalPageCount() {
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPageSize() {
    return maxRows;
  }

  @Override
  public int getPageIndex() {
    return getPageSize() != 0 ? getOffset() / getPageSize() : 0;
  }

  public int getOffset() {
    return firstRow;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    return hasNext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasPrev() {
    return firstRow > 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDisplayXtoYofZ(String to, String of) {
    int first = firstRow + 1;
    int last = firstRow + getList().size();
    int total = getTotalCount();

    return first + to + last + of + total;
  }
}
