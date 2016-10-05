package services;

import com.google.inject.ImplementedBy;

import models.ProjectUser;
import services.impl.ProjectUserServiceImpl;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(ProjectUserServiceImpl.class)
public interface ProjectUserService extends ModelService<ProjectUser>
{
}
