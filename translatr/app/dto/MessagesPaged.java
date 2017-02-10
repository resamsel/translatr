package dto;

import java.util.function.Function;

import com.avaje.ebean.PagedList;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class MessagesPaged extends DtoPagedList<models.Message, Message> {
  /**
   * @param delegate
   * @param mapper
   * 
   */
  public MessagesPaged(PagedList<models.Message> delegate,
      Function<models.Message, Message> mapper) {
    super(delegate, mapper);
  }
}
