package mappers;

import dto.ProjectUser;
import models.Project;
import models.User;

public class ProjectUserMapper {
  public static models.ProjectUser toModel(ProjectUser in) {
    models.ProjectUser out = new models.ProjectUser();

    out.project = new Project()
        .withId(in.projectId)
        .withName(in.projectName)
        .withOwner(new User().withUsername(in.projectOwnerUsername));
    out.user = new User().withId(in.userId).withUsername(in.userUsername);
    out.role = ProjectRoleMapper.toModel(in.role);
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;

    return out;
  }

  public static ProjectUser toDto(models.ProjectUser in) {
    ProjectUser out = new ProjectUser();

    out.id = in.id;
    out.role = ProjectRoleMapper.toDto(in.role);
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;

    out.projectId = in.project.id;
    out.projectName = in.project.name;
    if (in.project.owner != null) {
      out.projectOwnerUsername = in.project.owner.username;
    }

    out.userId = in.user.id;
    out.userName = in.user.name;
    out.userUsername = in.user.username;
    out.userEmail = in.user.email;

    return out;
  }
}
