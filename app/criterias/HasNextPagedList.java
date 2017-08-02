package criterias;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.Monitor;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import javax.persistence.Transient;

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

  private HasNextPagedList(Query<T> query) {
    this.query = query;
    this.maxRows = query.getMaxRows();
    this.firstRow = query.getFirstRow();
  }

  private HasNextPagedList(List<T> list) {
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
  public void loadRowCount() {
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

  public static <T> HasNextPagedList<T> create(Query<T> query) {
    HasNextPagedList<T> pagedList = new HasNextPagedList<>(query);

    pagedList.getList();

    return pagedList;
  }

  public static <T> HasNextPagedList<T> create(ExpressionList<T> expressionList) {
    return create(expressionList.query());
  }

  public static <T> HasNextPagedList<T> create(List<T> list) {
    return new HasNextPagedList<>(list);
  }

  public static <T> HasNextPagedList<T> create(T... a) {
    return new HasNextPagedList<>(Arrays.asList(a));
  }
}
