package forms;

import models.Project;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import validators.ProjectName;

/**
 * @author resamsel
 * @version 2 Sep 2016
 */
public class ProjectForm {

  @Constraints.Required
  @Constraints.MaxLength(Project.NAME_LENGTH)
  @Constraints.Pattern("[a-zA-Z0-9_\\.-]*")
  @ProjectName
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Project fill(Project in) {
    in.name = name;

    return in;
  }

  public static ProjectForm from(Project in) {
    ProjectForm out = new ProjectForm();

    out.name = in.name;

    return out;
  }

  public static Form<ProjectForm> form(FormFactory formFactory) {
    return formFactory.form(ProjectForm.class);
  }
}