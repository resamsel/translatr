package services;

import java.util.UUID;

import com.google.inject.ImplementedBy;

import models.Project;
import models.User;
import services.impl.ProjectServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(ProjectServiceImpl.class)
public interface ProjectService extends ModelService<Project>
{
	/**
	 * @param user
	 * @param name
	 * @return
	 */
	Project getByOwnerAndName(User user, String name);

	/**
	 * @param id
	 * @return
	 */
	Project getById(UUID id);
}
