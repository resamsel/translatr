package models;

import com.avaje.ebean.annotation.DbEnumValue;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum FeatureFlag {
  ProjectCliCard("project-cli-card", false),
  HeaderGraphic("header-graphic", false);

  private static final Map<String, FeatureFlag> MAP = Stream.of(FeatureFlag.values())
          .collect(toMap(FeatureFlag::getName, x -> x));

  private final String name;
  private final boolean enabled;

  FeatureFlag(String name, boolean enabled) {
    this.name = name;
    this.enabled = enabled;
  }

  @DbEnumValue
  public String getName() {
    return name;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public static FeatureFlag of(String featureFlagName) {
    return MAP.get(featureFlagName);
  }
}
