package criterias;

import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;

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
      PagedList<T> pagedList = query.findPagedList();

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
