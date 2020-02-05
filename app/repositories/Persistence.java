package repositories;

import com.avaje.ebean.Query;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.google.inject.ImplementedBy;
import repositories.impl.PersistenceImpl;

import java.util.Collection;
import java.util.function.Consumer;

@ImplementedBy(PersistenceImpl.class)
public interface Persistence {
  RepositoryProvider getRepositoryProvider();

  boolean isNew(Object t);

  void save(Object t);

  int saveAll(Collection<?> t);

  void batchExecute(Consumer<Transaction> consumer) throws Exception;

  boolean delete(Object t);

  int deleteAll(Collection<?> t);

  void markAsDirty(Object t);

  void refresh(Object t);

  <T> Query<T> createQuery(Class<T> clazz);

  String getDatabasePlatformName();

  SqlUpdate createSqlUpdate(String sql);
}
