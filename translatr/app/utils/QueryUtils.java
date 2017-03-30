package utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.avaje.ebean.Query;

/**
 * @author resamsel
 * @version 24 Mar 2017
 */
public class QueryUtils {
  public static <T> Query<T> fetch(Query<T> query, List<String> propertiesToFetch,
      Map<String, List<String>> fetchMap) {
    return fetch(query, propertiesToFetch.stream().filter(fetch -> fetchMap.containsKey(fetch))
        .map(fetch -> fetchMap.get(fetch)).flatMap(fetches -> fetches.stream()));
  }

  public static <T> Query<T> fetch(Query<T> query, List<String> propertiesToFetch) {
    return fetch(query, propertiesToFetch.stream());
  }

  public static <T> Query<T> fetch(Query<T> query, Stream<String> propertiesToFetch) {
    propertiesToFetch.forEach(fetch -> query.fetch(fetch));

    return query;
  }
}