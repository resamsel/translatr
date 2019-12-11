package repositories;

import com.avaje.ebean.Model;
import com.google.inject.ImplementedBy;
import models.ProjectUser;
import repositories.impl.RepositoryProviderImpl;

@ImplementedBy(RepositoryProviderImpl.class)
public interface RepositoryProvider {
  Model.Find<Long, ProjectUser> getProjectUserRepository();
}
