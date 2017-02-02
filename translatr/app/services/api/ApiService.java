package services.api;

import java.util.List;
import java.util.function.Consumer;

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
  List<T> find(CRITERIA criteria, @SuppressWarnings("unchecked") Consumer<CRITERIA>... validators);

  T get(ID id);

  T create(JsonNode in);

  T update(JsonNode in);

  T delete(ID id);
}
