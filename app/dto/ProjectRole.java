package dto;

public enum ProjectRole {
  Owner, Manager, Developer, Translator;

  public static ProjectRole from(models.ProjectRole role) {
    return ProjectRole.valueOf(role.name());
  }

  public models.ProjectRole toModel() {
    return models.ProjectRole.valueOf(name());
  }
}
