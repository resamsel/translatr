package mappers;

import dto.ProjectRole;

public class ProjectRoleMapper {
  public static ProjectRole toDto(models.ProjectRole role) {
    return ProjectRole.valueOf(role.name());
  }

  public static models.ProjectRole toModel(ProjectRole in) {
    return models.ProjectRole.valueOf(in.name());
  }
}
