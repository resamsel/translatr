package repositories.impl;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import play.api.db.evolutions.DynamicEvolutions;
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
  private final DynamicEvolutions dynamicEvolutions;

  @Inject
  public PersistenceImpl(EbeanConfig ebeanConfig, DynamicEvolutions dynamicEvolutions) {
    this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    this.dynamicEvolutions = dynamicEvolutions;
  }

  @Override
  public <T> Query<T> find(Class<T> beanType) {
    return ebeanServer.find(beanType);
  }

  @Override
  public boolean isNew(Object t) {
    return ebeanServer.getBeanState(t).isNew();
  }

  @Override
  public void save(Object t) {
    ebeanServer.save(t);
  }

  @Override
  public int saveAll(Collection<?> t) {
    return ebeanServer.saveAll(t);
  }

  @Override
  public <T> T update(T t) {
    ebeanServer.update(t);

    return t;
  }

  @Override
  public void batchExecute(Consumer<Transaction> consumer) throws Exception {
    TransactionUtils.batchExecute(consumer);
  }

  @Override
  public boolean delete(Object t) {
    return ebeanServer.delete(t);
  }

  @Override
  public int deleteAll(Collection<?> t) {
    return ebeanServer.deleteAll(t);
  }

  @Override
  public void markAsDirty(Object t) {
    ebeanServer.markAsDirty(t);
  }

  @Override
  public void refresh(Object t) {
    ebeanServer.refresh(t);
  }

  @Override
  public <T> Query<T> createQuery(Class<T> clazz) {
    return ebeanServer.createQuery(clazz);
  }

  @Override
  public String getDatabasePlatformName() {
    return ebeanServer.getPluginApi().getDatabasePlatform().getName();
  }

  @Override
  public SqlUpdate createSqlUpdate(String sql) {
    return ebeanServer.createSqlUpdate(sql);
  }
}
