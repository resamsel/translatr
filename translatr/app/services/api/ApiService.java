package services.api;

import java.util.function.Consumer;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.JsonNode;

import criterias.AbstractSearchCriteria;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
public interface ApiService<T, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>> {
  /**
   * @param criteria
   * @param validators
   * @return
   */
  @SuppressWarnings("unchecked")
  PagedList<T> find(CRITERIA criteria, Consumer<CRITERIA>... validators);

  T get(ID id, String... propertiesToFetch);

  T create(JsonNode in);

  T update(JsonNode in);

  T delete(ID id);
}
