package services;

import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import java.util.UUID;
import models.Project;
import models.User;
import services.impl.ProjectServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(ProjectServiceImpl.class)
public interface ProjectService extends ModelService<Project, UUID, ProjectCriteria> {
  /**
   * @param username The username of the project owner
   * @param name The name of the project
   * @return
   */
  Project byOwnerAndName(String username, String name, String... fetches);

  /**
   * @param projectId
   * @param wordCountDiff
   */
  void increaseWordCountBy(UUID projectId, int wordCountDiff);

  /**
   * @param projectId
   */
  void resetWordCount(UUID projectId);
}
