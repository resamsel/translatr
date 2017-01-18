package services;

import java.util.Collection;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
public interface ModelService<T> {
  T create(T model);

  T update(T model);

  T save(T t);

  Collection<T> save(Collection<T> t);

  void delete(T t);

  void delete(Collection<T> t);
}
