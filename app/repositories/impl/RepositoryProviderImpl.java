package repositories.impl;

import com.avaje.ebean.Model;
import models.ProjectUser;
import repositories.RepositoryProvider;

import javax.inject.Singleton;

@Singleton
public class RepositoryProviderImpl implements RepositoryProvider {
  @Override
  public Model.Find<Long, ProjectUser> getProjectUserRepository() {
    return new Model.Find<Long, ProjectUser>() {
    };
  }
}
