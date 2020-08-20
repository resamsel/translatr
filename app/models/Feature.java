package models;

import io.ebean.annotation.DbEnumValue;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum Feature {
  ProjectCliCard("project-cli-card", true),
  HeaderGraphic("header-graphic", true),
  LanguageSwitcher("language-switcher", true);

  private static final Map<String, Feature> MAP = Stream.of(Feature.values())
          .collect(toMap(Feature::getName, x -> x));

  private final String name;
  private final boolean enabled;

  Feature(String name, boolean enabled) {
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

  public static Feature of(String name) {
    return MAP.get(name);
  }
}
