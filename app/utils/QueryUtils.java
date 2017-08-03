package utils;

import com.avaje.ebean.Query;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author resamsel
 * @version 24 Mar 2017
 */
public class QueryUtils {
  public static <T> Query<T> fetch(Query<T> query, Collection<String> propertiesToFetch,
      Map<String, List<String>> fetchMap) {
    return fetch(query, propertiesToFetch.stream().filter(fetchMap::containsKey).map(fetchMap::get)
        .flatMap(Collection::stream));
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

  public static Set<String> mergeFetches(String[] propertiesToFetch, String... fetches) {
    if(propertiesToFetch.length > 0)
      return mergeFetches(Arrays.asList(propertiesToFetch), fetches);

    return new HashSet<>(Arrays.asList(fetches));
  }

  public static Set<String> mergeFetches(List<String> propertiesToFetch, String... fetches) {
    if (fetches.length > 0)
      return mergeFetches(propertiesToFetch, Arrays.asList(fetches));

    return new HashSet<>(propertiesToFetch);
  }

  public static Set<String> mergeFetches(List<String> propertiesToFetch,
      Collection<String> fetches) {
    HashSet<String> fetchSet = new HashSet<>(propertiesToFetch);
    if (fetches != null && fetches.size() > 0)
      fetchSet.addAll(fetches);
    return fetchSet;
  }
}
