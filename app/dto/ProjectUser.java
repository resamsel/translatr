package dto;

import controllers.AbstractController;
import controllers.routes;
import models.Project;
import models.User;
import org.joda.time.DateTime;
import play.mvc.Call;

import java.util.UUID;

public class ProjectUser extends Dto {

  private static final long serialVersionUID = 523759011096520257L;

  public Long id;
  public DateTime whenCreated;
  public DateTime whenUpdated;

  public ProjectRole role;

  public UUID projectId;
  public String projectName;
  public String projectOwnerUsername;

  public UUID userId;
  public String userName;
  public String userUsername;
  public String userEmail;

  public ProjectUser() {
  }

  private ProjectUser(models.ProjectUser in) {
    this.id = in.id;
    this.role = ProjectRole.from(in.role);
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;

    this.projectId = in.project.id;
    this.projectName = in.project.name;
    if (in.project.owner != null) {
      this.projectOwnerUsername = in.project.owner.username;
    }

    this.userId = in.user.id;
    this.userName = in.user.name;
    this.userUsername = in.user.username;
    this.userEmail = in.user.email;
  }

  public models.ProjectUser toModel() {
    models.ProjectUser out = new models.ProjectUser();

    out.project = new Project()
        .withId(projectId)
        .withName(projectName)
        .withOwner(new User().withUsername(projectOwnerUsername));
    out.user = new User().withId(userId).withUsername(userUsername);
    out.role = role.toModel();
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;

    return out;
  }

  public Call route() {
    return routes.Projects.membersBy(projectOwnerUsername, projectName,
        AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  public static ProjectUser from(models.ProjectUser in) {
    return new ProjectUser(in);
  }
}
