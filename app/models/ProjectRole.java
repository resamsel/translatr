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
 * @version 5 Oct 2016
 */
public enum ProjectRole {
  Owner, Manager, Developer, Translator;

  private static final Map<String, ProjectRole> PROJECT_ROLE_MAP =
      Arrays.stream(ProjectRole.values())
          .collect(toMap(ProjectRole::name, Function.identity()));

  public static List<ProjectRole> fromQueryParam(String roles) {
    if (roles == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(roles.split(","))
        .map(ProjectRole::fromString)
        .filter(Objects::nonNull)
        .collect(toList());
  }

  private static ProjectRole fromString(String s) {
    return PROJECT_ROLE_MAP.get(s);
  }
}
