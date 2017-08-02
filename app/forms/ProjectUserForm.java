package forms;

import models.ProjectRole;
import models.ProjectUser;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import validators.UserByUsername;

/**
 * @author resamsel
 * @version 2 Sep 2016
 */
public class ProjectUserForm {

  @Constraints.Required
  @Constraints.MaxLength(User.USERNAME_LENGTH)
  @UserByUsername
  private String username;

  @Constraints.Required
  private ProjectRole role;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public ProjectRole getRole() {
    return role;
  }

  public void setRole(ProjectRole role) {
    this.role = role;
  }

  public ProjectUser fill(ProjectUser in) {
    in.role = role;

    return in;
  }

  public static Form<ProjectUserForm> form(FormFactory formFactory) {
    return formFactory.form(ProjectUserForm.class);
  }
}
