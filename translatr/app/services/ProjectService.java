package services;

import java.util.UUID;

import com.google.inject.ImplementedBy;

import criterias.ProjectCriteria;
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
   * @param user
   * @param name
   * @return
   */
  Project byOwnerAndName(User user, String name);
}
