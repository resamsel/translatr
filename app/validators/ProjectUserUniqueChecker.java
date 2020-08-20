package validators;

import io.ebean.PagedList;
import criterias.ProjectUserCriteria;
import models.ProjectUser;
import repositories.ProjectUserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class ProjectUserUniqueChecker implements NameUniqueChecker {

  private final ProjectUserRepository projectUserRepository;

  @Inject
  public ProjectUserUniqueChecker(ProjectUserRepository projectUserRepository) {
    this.projectUserRepository = projectUserRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof ProjectUser)) {
      return false;
    }

    ProjectUser t = (ProjectUser) o;
    PagedList<ProjectUser> existing = projectUserRepository.findBy(
        new ProjectUserCriteria().withProjectId(t.project.id).withUserId(t.user.id));

    return existing == null || existing.getList().size() == 0 || Objects.equals(existing.getList().get(0).id, t.id);
  }
}
