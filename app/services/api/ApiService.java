package services.api;

import com.fasterxml.jackson.databind.JsonNode;
import criterias.AbstractSearchCriteria;
import io.ebean.PagedList;
import play.mvc.Http;

import java.util.function.Consumer;

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
  PagedList<T> find(CRITERIA criteria, Consumer<CRITERIA> validator);

  /**
   * @param criteria
   * @param validators
   * @return
   */
  PagedList<T> find(CRITERIA criteria);

  T get(Http.Request request, ID id, String... propertiesToFetch);

  T create(Http.Request request, JsonNode in);

  T update(Http.Request request, JsonNode in);

  T delete(Http.Request request, ID id);
}
