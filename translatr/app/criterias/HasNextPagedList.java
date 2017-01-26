package criterias;

import java.util.List;
import java.util.concurrent.Future;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.Monitor;

/**
 * @author resamsel
 * @version 24 Jan 2017
 */
public class HasNextPagedList<T> implements PagedList<T> {
  private final Query<T> query;
  private final int maxRows;
  private final int firstRow;

  private final Monitor monitor = new Monitor();

  private List<T> list;
  private boolean hasNext;

  /**
   * Construct with firstRow/maxRows.
   */
  public HasNextPagedList(Query<T> query) {
    this.query = query;
    this.maxRows = query.getMaxRows();
    this.firstRow = query.getFirstRow();
  }

  /**
   * @param expressionList
   */
  public HasNextPagedList(ExpressionList<T> expressionList) {
    this(expressionList.query());
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
  public void loadRowCount() {}

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
  public Future<Integer> getFutureRowCount() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<T> getList() {
    synchronized (monitor) {
      if (list == null) {
        list = query.findList();

        if (hasNext = maxRows > 0 && list.size() > maxRows)
          list.subList(maxRows, list.size()).clear();
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
  public int getTotalRowCount() {
    return getTotalCount();
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
