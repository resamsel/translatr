package dto;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.AbstractController;
import controllers.routes;
import models.Project;
import models.ProjectRole;
import models.User;
import play.mvc.Call;

public class ProjectUser extends Dto {

  private static final long serialVersionUID = 523759011096520257L;

  public UUID projectId;
  public String projectName;
  public String projectOwnerUsername;

  public UUID userId;
  public String userUsername;

  public ProjectRole role;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  public ProjectUser() {
  }

  private ProjectUser(models.ProjectUser in) {
    this.projectId = in.project.id;
    this.projectName = in.project.name;
    this.projectOwnerUsername = in.project.owner.username;
    this.userId = in.user.id;
    this.userUsername = in.user.username;
    this.role = in.role;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
  }

  public models.ProjectUser toModel() {
    models.ProjectUser out = new models.ProjectUser();

    out.project = new Project().withId(projectId).withName(projectName)
        .withOwner(new User().withUsername(projectOwnerUsername));
    out.user = new User().withId(userId).withUsername(userUsername);
    out.role = role;
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
