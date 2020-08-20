package repositories;

import com.google.inject.ImplementedBy;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import repositories.impl.PersistenceImpl;

import java.util.Collection;
import java.util.function.Consumer;

@ImplementedBy(PersistenceImpl.class)
public interface Persistence {
  <T> Query<T> find(Class<T> beanType);

  boolean isNew(Object t);

  void save(Object t);

  int saveAll(Collection<?> t);

  <T> T update(T t);

  void batchExecute(Consumer<Transaction> consumer) throws Exception;

  boolean delete(Object t);

  int deleteAll(Collection<?> t);

  void markAsDirty(Object t);

  void refresh(Object t);

  <T> Query<T> createQuery(Class<T> clazz);

  String getDatabasePlatformName();

  SqlUpdate createSqlUpdate(String sql);
}
