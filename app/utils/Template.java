package utils;

import models.User;
import play.mvc.Call;

/**
 * @author resamsel
 * @version 7 Sep 2016
 */
public class Template {
  public final User loggedInUser;

  public String section;

  public String title;

  public String name;

  public Call backLink;

  public boolean notificationsEnabled;

  /**
   * @param auth
   */
  private Template(User loggedInUser) {
    this.loggedInUser = loggedInUser;
  }

  public Template withSection(String section) {
    this.section = section;
    return this;
  }

  public Template withTitle(String title) {
    this.title = title;
    return this;
  }

  public Template withName(String name) {
    this.name = name;
    return this;
  }

  public Template withBackLink(Call backLink) {
    this.backLink = backLink;
    return this;
  }

  public Template withNotificationsEnabled(boolean notificationsEnabled) {
    this.notificationsEnabled = notificationsEnabled;
    return this;
  }

  public static Template create(User user) {
    return new Template(user);
  }
}
