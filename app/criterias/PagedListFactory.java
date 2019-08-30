package criterias;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;

import java.util.Arrays;
import java.util.List;

public class PagedListFactory {

  private static final boolean useHasNextPagedList = false;

  public static <T> PagedList<T> create(Query<T> query) {
    if (useHasNextPagedList) {
      HasNextPagedList<T> pagedList = new HasNextPagedList<>(query);

      pagedList.getList();

      return pagedList;
    }

    return Ebean.getDefaultServer().findPagedList(query, Ebean.currentTransaction());
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
