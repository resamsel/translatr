package utils;

import org.apache.commons.lang3.StringUtils;

public class CacheUtils {
  public static String getCacheKey(String prefix, Object id, String... fetches) {
    if (id == null) {
      return null;
    }

    if (fetches.length > 0) {
      return String.format(
          "%s:%s:%s",
          prefix,
          String.valueOf(id),
          StringUtils.join(fetches, ":")
      );
    }

    return String.format("%s:%s", prefix, String.valueOf(id));
  }
}
