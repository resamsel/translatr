package dto;

import java.util.function.Function;

import com.avaje.ebean.PagedList;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class UsersPaged extends DtoPagedList<models.User, User> {
  /**
   * @param delegate
   * @param mapper
   * 
   */
  public UsersPaged(PagedList<models.User> delegate, Function<models.User, User> mapper) {
    super(delegate, mapper);
  }
}
