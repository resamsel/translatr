package repositories.impl;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import repositories.Persistence;
import utils.TransactionUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.function.Consumer;

@Singleton
public class PersistenceImpl implements Persistence {
  private EbeanServer ebeanServer;

  @Inject
  public PersistenceImpl() {
  }

  @Override
  public <T> Query<T> find(Class<T> beanType) {
    return getEbeanServer().find(beanType);
  }

  @Override
  public boolean isNew(Object t) {
    return getEbeanServer().getBeanState(t).isNew();
  }

  @Override
  public void save(Object t) {
    getEbeanServer().save(t);
  }

  @Override
  public int saveAll(Collection<?> t) {
    return getEbeanServer().saveAll(t);
  }

  @Override
  public <T> T update(T t) {
    getEbeanServer().update(t);

    return t;
  }

  @Override
  public void batchExecute(Consumer<Transaction> consumer) throws Exception {
    TransactionUtils.batchExecute(consumer);
  }

  @Override
  public boolean delete(Object t) {
    return getEbeanServer().delete(t);
  }

  @Override
  public int deleteAll(Collection<?> t) {
    return getEbeanServer().deleteAll(t);
  }

  @Override
  public void markAsDirty(Object t) {
    getEbeanServer().markAsDirty(t);
  }

  @Override
  public void refresh(Object t) {
    getEbeanServer().refresh(t);
  }

  @Override
  public <T> Query<T> createQuery(Class<T> clazz) {
    return getEbeanServer().createQuery(clazz);
  }

  @Override
  public String getDatabasePlatformName() {
    return getEbeanServer().getPluginApi().getDatabasePlatform().getName();
  }

  @Override
  public SqlUpdate createSqlUpdate(String sql) {
    return getEbeanServer().createSqlUpdate(sql);
  }

  private EbeanServer getEbeanServer() {
    if (ebeanServer == null) {
      synchronized (this) {
        if (ebeanServer == null) {
          ebeanServer = Ebean.getDefaultServer();
        }
      }
    }

    return ebeanServer;
  }
}
