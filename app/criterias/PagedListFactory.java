package criterias;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;

import java.util.Arrays;
import java.util.List;

public class PagedListFactory {

  private static final boolean includeCount = true;
  private static final int DEFAULT_LIMIT = 200;

  public <T> PagedList<T> createPagedList(Query<T> query, boolean includeCount) {
    return create(query, includeCount);
  }

  public <T> PagedList<T> createPagedList(Query<T> query) {
    return create(query, includeCount);
  }

  public static <T> PagedList<T> create(Query<T> query, boolean includeCount) {
    if (query.getMaxRows() <= 0) {
      query.setMaxRows(DEFAULT_LIMIT);
    }

    if (includeCount) {
      PagedList<T> pagedList = Ebean.getDefaultServer().findPagedList(query, Ebean.currentTransaction());

      pagedList.getList();
      pagedList.loadCount();

      return pagedList;
    }

    HasNextPagedList<T> pagedList = new HasNextPagedList<>(query);

    pagedList.getList();

    return pagedList;
  }

  public static <T> PagedList<T> create(Query<T> query) {
    return create(query, includeCount);
  }

  public static <T> PagedList<T> create(ExpressionList<T> expressionList, boolean includeCount) {
    return create(expressionList.query(), includeCount);
  }

  public static <T> PagedList<T> create(ExpressionList<T> expressionList) {
    return create(expressionList.query());
  }

  public static <T> PagedList<T> create(List<T> list) {
    return new HasNextPagedList<>(list);
  }

  @SafeVarargs
  public static <T> PagedList<T> create(T... a) {
    return create(Arrays.asList(a));
  }
}
