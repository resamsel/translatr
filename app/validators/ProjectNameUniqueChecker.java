package validators;

import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ProjectService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class ProjectNameUniqueChecker implements NameUniqueChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectNameUniqueChecker.class);

  private final ProjectService projectService;

  @Inject
  public ProjectNameUniqueChecker(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof Project)) {
      return false;
    }

    Project t = (Project) o;
    Project existing = projectService.byOwnerAndName(t.owner.username, t.name, null);

    LOGGER.debug("existing project: {}", existing);

    return existing == null || existing.equals(t);
  }
}
