package services;

import com.google.inject.ImplementedBy;

import models.ProjectUser;
import services.impl.ProjectUserServiceImpl;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(ProjectUserServiceImpl.class)
public interface ProjectUserService extends ModelService<ProjectUser>
{
}
