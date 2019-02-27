package services;

import com.avaje.ebean.PagedList;
import criterias.AbstractSearchCriteria;
import java.util.Collection;
import models.Model;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
public interface ModelService<T extends Model<T, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>> {
  PagedList<T> findBy(CRITERIA criteria);

  T byId(ID id, String... propertiesToFetch);

  T create(T model);

  T update(T model);

  Collection<T> save(Collection<T> t);

  void delete(T t);

  void delete(Collection<T> t);
}
