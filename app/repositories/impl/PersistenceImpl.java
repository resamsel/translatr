package repositories.impl;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import repositories.Persistence;
import repositories.RepositoryProvider;
import utils.TransactionUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.function.Consumer;

@Singleton
public class PersistenceImpl implements Persistence {
  private final RepositoryProvider repositoryProvider;

  @Inject
  public PersistenceImpl(RepositoryProvider repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public RepositoryProvider getRepositoryProvider() {
    return repositoryProvider;
  }

  @Override
  public boolean isNew(Object t) {
    return Ebean.getBeanState(t).isNew();
  }

  @Override
  public void save(Object t) {
    Ebean.save(t);
  }

  @Override
  public int saveAll(Collection<?> t) {
    return Ebean.saveAll(t);
  }

  @Override
  public void batchExecute(Consumer<Transaction> consumer) throws Exception {
    TransactionUtils.batchExecute(consumer);
  }

  @Override
  public boolean delete(Object t) {
    return Ebean.delete(t);
  }

  @Override
  public int deleteAll(Collection<?> t) {
    return Ebean.deleteAll(t);
  }

  @Override
  public void markAsDirty(Object t) {
    Ebean.markAsDirty(t);
  }

  @Override
  public void refresh(Object t) {
    Ebean.refresh(t);
  }

  @Override
  public <T> Query<T> createQuery(Class<T> clazz) {
    return Ebean.createQuery(clazz);
  }

  @Override
  public String getDatabasePlatformName() {
    return Ebean.getDefaultServer().getPluginApi().getDatabasePlatform().getName();
  }

  @Override
  public SqlUpdate createSqlUpdate(String sql) {
    return Ebean.createSqlUpdate(sql);
  }
}
