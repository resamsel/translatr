package repositories;

import com.avaje.ebean.Transaction;
import com.google.inject.ImplementedBy;
import models.Project;
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
}
