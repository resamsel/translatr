package validators;

import javax.inject.Inject;
import javax.inject.Singleton;
import models.Project;
import repositories.ProjectRepository;
import services.ProjectService;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class ProjectNameUniqueChecker implements NameUniqueChecker {

  private final ProjectRepository projectRepository;

  @Inject
  public ProjectNameUniqueChecker(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof Project)) {
      return false;
    }

    Project t = (Project) o;
    Project existing = projectRepository.byOwnerAndName(t.owner.username, t.name);

    return existing == null || existing.equals(t);
  }
}
