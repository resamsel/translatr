package mappers;

import dto.ProjectRole;

public class ProjectRoleMapper {
  public static ProjectRole toDto(models.ProjectRole in) {
    if (in == null) {
      return null;
    }

    return ProjectRole.valueOf(in.name());
  }

  public static models.ProjectRole toModel(ProjectRole in) {
    if (in == null) {
      return null;
    }

    return models.ProjectRole.valueOf(in.name());
  }
}
