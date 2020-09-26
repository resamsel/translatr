package services;

import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import models.Project;
import models.User;
import play.mvc.Http;
import services.impl.ProjectServiceImpl;

import java.util.UUID;

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
  Project byOwnerAndName(String username, String name, Http.Request request, String... fetches);

  Project increaseWordCountBy(UUID projectId, int wordCountDiff, Http.Request request);

  Project resetWordCount(UUID projectId, Http.Request request);

  /**
   * Changes the owner of the project to the given owner.
   *  @param project The project to change the owner of
   * @param owner The new owner
   * @return
   */
  Project changeOwner(Project project, User owner, Http.Request request);
}
