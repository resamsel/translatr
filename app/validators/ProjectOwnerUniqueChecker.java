package validators;

import io.ebean.PagedList;
import criterias.ProjectCriteria;
import forms.ProjectOwnerForm;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ProjectService;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

/**
 * @author resamsel
 * @version 22 Aug 2017
 */
@Singleton
public class ProjectOwnerUniqueChecker implements NameUniqueChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectOwnerUniqueChecker.class);

  private final ProjectService projectService;

  @Inject
  public ProjectOwnerUniqueChecker(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof ProjectOwnerForm)) {
      return false;
    }

    ProjectOwnerForm t = (ProjectOwnerForm) o;
    PagedList<Project> projects = projectService.findBy(
        new ProjectCriteria()
            .withOwnerId(t.getOwnerId())
            .withName(requireNonNull(t.getProjectName(), "project name"))
    );

    LOGGER.debug("existing projects: {}", projects != null ? projects.getList() : null);

    return projects == null || projects.getList().isEmpty();
  }
}
