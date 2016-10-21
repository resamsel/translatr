package models;

import java.util.HashMap;
import java.util.Map;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public enum Scope {
  ProjectAdmin(ScopeSection.Project, ScopeType.Admin),

  ProjectRead(ScopeSection.Project, ScopeType.Read),

  ProjectWrite(ScopeSection.Project, ScopeType.Write),

  LocaleAdmin(ScopeSection.Locale, ScopeType.Admin),

  LocaleRead(ScopeSection.Locale, ScopeType.Read),

  LocaleWrite(ScopeSection.Locale, ScopeType.Write);

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

  public String scope() {
    return String.format("%s:%s", section, type).toLowerCase();
  }

  public static Scope fromString(String scope) {
    return MAP.get(scope);
  }
}
