package repositories.impl;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import play.db.ebean.EbeanConfig;
import repositories.Persistence;
import utils.TransactionUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.function.Consumer;

@Singleton
public class PersistenceImpl implements Persistence {
  private final EbeanServer ebeanServer;

  @Inject
  public PersistenceImpl(EbeanConfig ebeanConfig) {
    this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
  }

  @Override
  public <T> Query<T> find(Class<T> beanType) {
    return ebeanServer.find(beanType);
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
  public <T> T update(T t) {
    Ebean.update(t);

    return t;
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
