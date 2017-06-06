package utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.avaje.ebean.Query;

/**
 * @author resamsel
 * @version 24 Mar 2017
 */
public class QueryUtils {
  public static <T> Query<T> fetch(Query<T> query, Collection<String> propertiesToFetch,
      Map<String, List<String>> fetchMap) {
    return fetch(query, propertiesToFetch.stream().filter(fetchMap::containsKey).map(fetchMap::get)
        .flatMap(fetches -> fetches.stream()));
  }

  public static <T> Query<T> fetch(Query<T> query, String... propertiesToFetch) {
    return fetch(query, Arrays.stream(propertiesToFetch));
  }

  public static <T> Query<T> fetch(Query<T> query, Collection<String> propertiesToFetch) {
    return fetch(query, propertiesToFetch.stream());
  }

  public static <T> Query<T> fetch(Query<T> query, Stream<String> propertiesToFetch) {
    propertiesToFetch.forEach(query::fetch);

    return query;
  }
}
