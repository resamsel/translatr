package models;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
public enum ActionType {
  Create, Update, Delete, Login, Logout;

  /**
   * @return
   */
  public String normalize() {
    return name().toLowerCase();
  }
}
