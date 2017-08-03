package repositories;

import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import java.util.UUID;
import models.Project;
import repositories.impl.ProjectRepositoryImpl;

@ImplementedBy(ProjectRepositoryImpl.class)
public interface ProjectRepository extends ModelRepository<Project, UUID, ProjectCriteria> {

  Project byOwnerAndName(String username, String name, String... fetches);
}
