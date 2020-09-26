package services;

import criterias.GetCriteria;
import io.ebean.PagedList;
import criterias.AbstractSearchCriteria;
import models.Model;
import play.mvc.Http;

import java.util.Collection;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
public interface ModelService<T extends Model<T, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>> {
  PagedList<T> findBy(CRITERIA criteria);

  T byId(ID id, Http.Request request, String... propertiesToFetch);

  T byId(GetCriteria<ID> criteria);

  T create(T model, Http.Request request);

  T update(T model, Http.Request request);

  Collection<T> save(Collection<T> t, Http.Request request);

  void delete(T t, Http.Request request);

  void delete(Collection<T> t, Http.Request request);
}
