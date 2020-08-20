package utils;

import io.ebean.Query;
import com.google.common.collect.ImmutableMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author resamsel
 * @version 24 Mar 2017
 */
public class QueryUtils {
  private static final Map<String, String> DIRECTION_MAP = ImmutableMap.of(
          "asc", "asc",
          "desc", "desc"
  );

  public static <T> Query<T> fetch(Query<T> query, String[] propertiesToFetch,
                                   Map<String, List<String>> fetchMap) {
    return fetch(query, Arrays.asList(propertiesToFetch), fetchMap);
  }

  public static <T> Query<T> fetch(Query<T> query, String... propertiesToFetch) {
    return fetch(query, Arrays.asList(propertiesToFetch));
  }

  public static <T> Query<T> fetch(Query<T> query, Collection<String> propertiesToFetch,
      Map<String, List<String>> fetchMap) {
    return fetch(query, propertiesToFetch.stream().filter(fetchMap::containsKey).map(fetchMap::get)
        .flatMap(Collection::stream));
  }

  public static <T> Query<T> fetch(Query<T> query, Collection<String> propertiesToFetch) {
    return fetch(query, propertiesToFetch.stream());
  }

  public static <T> Query<T> fetch(Query<T> query, Stream<String> propertiesToFetch) {
    propertiesToFetch.forEach(query::fetch);

    return query;
  }

  public static Set<String> mergeFetches(String[] propertiesToFetch, String... fetches) {
    if (propertiesToFetch.length > 0) {
      return mergeFetches(Arrays.asList(propertiesToFetch), fetches);
    }

    return new HashSet<>(Arrays.asList(fetches));
  }

  public static Set<String> mergeFetches(String[] propertiesToFetch, Collection<String> fetches) {
    if (propertiesToFetch.length > 0) {
      return mergeFetches(Arrays.asList(propertiesToFetch), fetches);
    }

    return new HashSet<>(fetches);
  }

  private static Set<String> mergeFetches(List<String> propertiesToFetch, String... fetches) {
    if (fetches.length > 0) {
      return mergeFetches(propertiesToFetch, Arrays.asList(fetches));
    }

    return new HashSet<>(propertiesToFetch);
  }

  private static Set<String> mergeFetches(List<String> propertiesToFetch,
                                          Collection<String> fetches) {
    HashSet<String> fetchSet = new HashSet<>(propertiesToFetch);
    if (fetches != null && fetches.size() > 0) {
      fetchSet.addAll(fetches);
    }
    return fetchSet;
  }

  /**
   * Supports one single order column.
   *
   * @param order the order to map
   * @return the mapped order string
   */
  @CheckForNull
  public static String mapOrder(@CheckForNull String order, @Nonnull Map<String, String> orderMap) {
    if (order == null) {
      return null;
    }

    String[] parts = order.split(" ");

    if (parts.length > 0) {
      String column = orderMap.get(parts[0]);

      if (column == null) {
        // column not found or not allowed
        return null;
      }

      String direction = "asc";
      if (parts.length > 1) {
        direction = DIRECTION_MAP.get(parts[1]);

        if (direction == null) {
          // direction not supported
          return null;
        }
      }

      return column + " " + direction;
    }

    return null;
  }
}
