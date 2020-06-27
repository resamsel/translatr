package dto;

import com.avaje.ebean.PagedList;

import java.util.function.Function;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class KeysPaged extends DtoPagedList<models.Key, Key> {
  private static final long serialVersionUID = -8200869140583550899L;

  /**
   * @param delegate
   * @param mapper
   * 
   */
  public KeysPaged(PagedList<models.Key> delegate, Function<models.Key, Key> mapper) {
    super(delegate, mapper);
  }
}
