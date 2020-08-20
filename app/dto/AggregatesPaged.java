package dto;

import io.ebean.PagedList;

import java.util.function.Function;

public class AggregatesPaged extends DtoPagedList<models.Aggregate, Aggregate> {
  /**
   * @param delegate
   * @param mapper
   */
  public AggregatesPaged(PagedList<models.Aggregate> delegate, Function<models.Aggregate, Aggregate> mapper) {
    super(delegate, mapper);
  }
}
