package utils;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;

import models.LogEntry;
import models.User;

/**
 *
 * @author resamsel
 * @version 7 Sep 2016
 */
public class Template {
  public final PlayAuthenticate auth;

  public final User loggedInUser;

  public String title;

  public String name;

  public PagedList<LogEntry> notifications;

  /**
   * @param auth
   */
  private Template(PlayAuthenticate auth, User loggedInUser) {
    this.auth = auth;
    this.loggedInUser = loggedInUser;
  }

  public Template withTitle(String title) {
    this.title = title;
    return this;
  }

  public Template withName(String name) {
    this.name = name;
    return this;
  }

  public Template withNotifications(PagedList<LogEntry> notifications) {
    this.notifications = notifications;
    return this;
  }

  public boolean hasUnreadNotifications() {
    return notifications != null && notifications.getList().stream().anyMatch(l -> l.unread);
  }

  public static Template create(PlayAuthenticate auth, User user) {
    return new Template(auth, user);
  }

  public static Template from(String title, String name) {
    return new Template(null, null).withTitle(title).withName(name);
  }

  public static Template from(String title) {
    return new Template(null, null).withTitle(title);
  }
}
