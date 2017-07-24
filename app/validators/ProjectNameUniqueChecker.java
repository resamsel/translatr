package validators;

import models.Project;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
public class ProjectNameUniqueChecker implements NameUniqueChecker {
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object o) {
    if (!(o instanceof Project))
      return false;

    Project t = (Project) o;

    return Project.byOwnerAndName(t.owner.username, t.name) == null;
  }
}
