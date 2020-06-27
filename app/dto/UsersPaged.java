package dto;

import com.avaje.ebean.PagedList;

import java.util.function.Function;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class UsersPaged extends DtoPagedList<models.User, User> {
  private static final long serialVersionUID = -2110566783351009448L;

  /**
   * @param delegate
   * @param mapper
   * 
   */
  public UsersPaged(PagedList<models.User> delegate, Function<models.User, User> mapper) {
    super(delegate, mapper);
  }
}
