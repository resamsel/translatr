package services.api.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import criterias.ProjectCriteria;
import models.Project;
import models.Scope;
import services.ProjectService;
import services.api.ProjectApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class ProjectApiServiceImpl extends
    AbstractApiService<Project, UUID, ProjectCriteria, dto.Project> implements ProjectApiService {
  /**
   * @param projectService
   */
  @Inject
  protected ProjectApiServiceImpl(ProjectService projectService) {
    super(projectService, dto.Project.class, dto.Project::from, Project::from,
        new Scope[] {Scope.ProjectRead}, new Scope[] {Scope.ProjectWrite});
  }
}
