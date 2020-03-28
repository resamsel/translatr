package dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum Feature {
  ProjectCliCard("project-cli-card"),
  HeaderGraphic("header-graphic"),
  LanguageSwitcher("language-switcher");

  private static final Map<String, Feature> MAP = Stream.of(Feature.values())
          .collect(toMap(Feature::getName, x -> x));

  private final String name;

  Feature(String name) {
    this.name = name;
  }

  public static Feature of(String name) {
    return MAP.get(name);
  }

  @JsonValue
  public String getName() {
    return name;
  }
}
