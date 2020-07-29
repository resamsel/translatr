package dto;

import org.joda.time.DateTime;

/**
 * @author resamsel
 * @version 23 May 2017
 */
public class AggregatedNotification extends Dto {

  private static final long serialVersionUID = -6020395415666005155L;

  public DateTime whenCreated;
  public String created;
  public DateTime whenUpdated;
  public String updated;
  public String title;
  public String subtitle;
  public String verb;
  public long activityCount;
  public long actorCount;
  public String contentType;
  public String name;
  public String icon;
  public String color;
  public String link;
  public User user;
  public Project project;
}
