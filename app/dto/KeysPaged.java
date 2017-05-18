package dto;

import java.util.function.Function;

import com.avaje.ebean.PagedList;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class KeysPaged extends DtoPagedList<models.Key, Key> {
  /**
   * @param delegate
   * @param mapper
   * 
   */
  public KeysPaged(PagedList<models.Key> delegate, Function<models.Key, Key> mapper) {
    super(delegate, mapper);
  }
}
