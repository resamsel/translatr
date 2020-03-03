package models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
public enum ActionType {
  Create, Update, Delete, Login, Logout;

  private static final Map<String, ActionType> ACTION_TYPE_MAP =
      Arrays.stream(ActionType.values())
          .collect(toMap(ActionType::name, Function.identity()));

  public static List<ActionType> fromQueryParam(String types) {
    if (types == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(types.split(","))
        .map(ActionType::fromString)
        .filter(Objects::nonNull)
        .collect(toList());
  }

  private static ActionType fromString(String s) {
    return ACTION_TYPE_MAP.get(s);
  }

  public String normalize() {
    return name().toLowerCase();
  }
}
