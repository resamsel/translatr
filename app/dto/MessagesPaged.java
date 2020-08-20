package dto;

import io.ebean.PagedList;

import java.util.function.Function;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class MessagesPaged extends DtoPagedList<models.Message, Message> {
  private static final long serialVersionUID = 5155380848902188888L;

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
