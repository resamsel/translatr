package dto;

import java.util.Date;
import java.util.UUID;

/**
 * @author resamsel
 * @version 23 May 2017
 */
public class Notification extends Dto {
  private static final long serialVersionUID = -2532807699451690607L;

  public String id;
  public Date time;
  public User user;
  public Project project;
  public String verb;
  public String title;
  public UUID activityId;
  public String contentType;
  public String name;
  public String icon;
  public String color;
  public String link;

  /**
   * FindBugs EI: getTime() may expose internal representation by returning PublishNotification.time.
   *
   * @return the time
   */
  public Date getTime() {
    return time != null ? new Date(time.getTime()) : null;
  }

  /**
   * FindBugs EI2: setTime(Date) may expose internal representation by storing an externally mutable
   * object into PublishNotification.time.
   *
   * @param time the time to set
   */
  public void setTime(Date time) {
    this.time = time != null ? new Date(time.getTime()) : null;
  }

  public static UUID extractUuid(String id) {
    String[] parts = id.split(":", 2);
    if (parts.length != 2)
      return null;

    return getUuid(parts[1]);
  }

  private static UUID getUuid(String uuid) {
    if (uuid == null || uuid.trim().length() < 1) {
      return null;
    }

    try {
      return UUID.fromString(uuid);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
