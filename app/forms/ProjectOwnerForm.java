package forms;

import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import validators.NameUnique;
import validators.ProjectOwnerUniqueChecker;

import java.util.UUID;

/**
 * @author resamsel
 * @version 20 Feb 2017
 */
@NameUnique(checker = ProjectOwnerUniqueChecker.class, message = "error.nameunique.ownerchange")
public class ProjectOwnerForm {

  @Constraints.Required
  private UUID projectId;

  @Constraints.Required
  private UUID ownerId;

  private String projectName;

  public UUID getProjectId() {
    return projectId;
  }

  public void setProjectId(UUID projectId) {
    this.projectId = projectId;
  }

  public ProjectOwnerForm withProjectId(UUID projectId) {
    setProjectId(projectId);
    return this;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public ProjectOwnerForm withProjectName(String projectName) {
    setProjectName(projectName);
    return this;
  }

  public static Form<ProjectOwnerForm> form(FormFactory formFactory) {
    return formFactory.form(ProjectOwnerForm.class);
  }
}
