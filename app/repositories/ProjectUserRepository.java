package repositories;

import com.google.inject.ImplementedBy;
import criterias.ProjectUserCriteria;
import models.ProjectUser;
import repositories.impl.ProjectUserRepositoryImpl;

@ImplementedBy(ProjectUserRepositoryImpl.class)
public interface ProjectUserRepository extends
    ModelRepository<ProjectUser, Long, ProjectUserCriteria> {

  int countBy(ProjectUserCriteria criteria);
}
