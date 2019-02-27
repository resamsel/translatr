package services;

import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import java.util.UUID;
import models.Project;
import models.User;
import services.impl.ProjectServiceImpl;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(ProjectServiceImpl.class)
public interface ProjectService extends ModelService<Project, UUID, ProjectCriteria> {

  /**
   * Get the project by the owner's username and the project name.
   *
   * @param username The username of the project owner
   * @param name The name of the project
   */
  Project byOwnerAndName(String username, String name, String... fetches);

  void increaseWordCountBy(UUID projectId, int wordCountDiff);

  void resetWordCount(UUID projectId);

  /**
   * Changes the owner of the project to the given owner.
   *
   * @param project The project to change the owner of
   * @param owner The new owner
   */
  void changeOwner(Project project, User owner);
}
