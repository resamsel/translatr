package utils;

/**
 *
 * @author resamsel
 * @version 30 Sep 2016
 */
public enum SessionKey {
  UserId("userId"),

  LastLogin("lastLogin"),

  LastAcknowledged("lastAck");

  private String key;

  SessionKey(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return key;
  }
}
