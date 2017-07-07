package services;

import com.google.inject.ImplementedBy;
import criterias.ProjectUserCriteria;
import models.ProjectUser;
import services.impl.ProjectUserServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(ProjectUserServiceImpl.class)
public interface ProjectUserService extends ModelService<ProjectUser, Long, ProjectUserCriteria> {
}
