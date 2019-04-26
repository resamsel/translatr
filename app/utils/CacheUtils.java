package utils;

import org.apache.commons.lang3.StringUtils;

public class CacheUtils {
  public static String getCacheKey(String prefix, Object id, String... fetches) {
    if (fetches.length > 0) {
      return String.format(
          "%s:%s:%s",
          prefix,
          id != null ? String.valueOf(id) : "",
          StringUtils.join(fetches, ":")
      );
    }

    return String.format("%s:%s", prefix, id != null ? String.valueOf(id) : "");
  }
}
