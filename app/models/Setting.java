package models;

import io.ebean.annotation.DbEnumValue;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum Setting {
  SaveBehavior("save-behavior", "save");

  private static final Map<String, Setting> MAP = Stream.of(Setting.values())
          .collect(toMap(Setting::getName, x -> x));

  private final String name;
  private final String value;

  Setting(String name, String value) {
    this.name = name;
    this.value = value;
  }

  @DbEnumValue
  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public static Setting of(String name) {
    return MAP.get(name);
  }
}
