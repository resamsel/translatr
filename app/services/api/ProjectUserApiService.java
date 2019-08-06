package services.api;

import com.google.inject.ImplementedBy;
import criterias.ProjectUserCriteria;
import dto.ProjectUser;
import services.api.impl.ProjectUserApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(ProjectUserApiServiceImpl.class)
public interface ProjectUserApiService extends ApiService<ProjectUser, Long, ProjectUserCriteria> {
}
