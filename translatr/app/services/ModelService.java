package services;

import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
public interface ModelService<T> {
  Class<?> getClazz();

  T create(JsonNode json);

  T update(JsonNode json);

  T save(T t);

  Collection<T> save(Collection<T> t);

  void delete(T t);

  void delete(Collection<T> t);
}
