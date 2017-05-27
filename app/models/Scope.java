package models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public enum Scope {
  UserRead(ScopeSection.User, ScopeType.Read),

  UserWrite(ScopeSection.User, ScopeType.Write),

  ProjectRead(ScopeSection.Project, ScopeType.Read),

  ProjectWrite(ScopeSection.Project, ScopeType.Write),

  LocaleRead(ScopeSection.Locale, ScopeType.Read),

  LocaleWrite(ScopeSection.Locale, ScopeType.Write),

  KeyRead(ScopeSection.Key, ScopeType.Read),

  KeyWrite(ScopeSection.Key, ScopeType.Write),

  MessageRead(ScopeSection.Message, ScopeType.Read),

  MessageWrite(ScopeSection.Message, ScopeType.Write),

  NotificationRead(ScopeSection.Notification, ScopeType.Read),

  NotificationWrite(ScopeSection.Notification, ScopeType.Write);

  private static final Map<String, Scope> MAP = new HashMap<>();

  private final ScopeSection section;
  private final ScopeType type;

  static {
    for (Scope scope : Scope.values())
      MAP.put(scope.scope(), scope);
  }

  /**
   * 
   */
  private Scope(ScopeSection section, ScopeType type) {
    this.section = section;
    this.type = type;
  }

  @JsonValue
  public String scope() {
    return String.format("%s:%s", type, section).toLowerCase();
  }

  public ScopeSection getSection() {
    return section;
  }

  public ScopeType getType() {
    return type;
  }

  public static Scope fromString(String scope) {
    return MAP.get(scope);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("%s:%s", type.name().toLowerCase(), section.name().toLowerCase());
  }
}
