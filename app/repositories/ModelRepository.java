package repositories;

import com.avaje.ebean.PagedList;
import criterias.AbstractSearchCriteria;
import java.util.Collection;
import models.Model;

public interface ModelRepository<T extends Model<T, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>> {
  PagedList<T> findBy(CRITERIA criteria);

  T byId(ID id, String... propertiesToFetch);

  T create(T model);

  T update(T model);

  /**
   * Persist model to database.
   */
  T save(T t);

  /**
   * Persist model collection to database.
   */
  Collection<T> save(Collection<T> t);

  /**
   * Persist model to database, ignoring any pre/post save methods.
   */
  T persist(T t);

  void delete(T t);

  void delete(Collection<T> t);

}