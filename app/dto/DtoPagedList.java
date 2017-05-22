package dto;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import criterias.HasNextPagedList;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class DtoPagedList<MODEL, DTO extends Dto> extends Dto implements PagedList<DTO> {
  private static final long serialVersionUID = -702426798092435389L;

  private List<DTO> list;

  @JsonIgnore
  private PagedList<MODEL> delegate;

  private int offset;
  private int limit;

  /**
   * 
   */
  public DtoPagedList(PagedList<MODEL> delegate, Function<MODEL, DTO> mapper) {
    this.delegate = delegate;
    this.list = delegate.getList().stream().map(mapper).collect(toList());

    if (delegate instanceof HasNextPagedList)
      this.offset = ((HasNextPagedList<?>) delegate).getOffset();
    this.limit = delegate.getPageSize();
  }

  public int getOffset() {
    return offset;
  }

  public int getLimit() {
    return limit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadCount() {}

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadRowCount() {}

  /**
   * {@inheritDoc}
   */
  @JsonIgnore
  @Override
  public Future<Integer> getFutureCount() {
    return delegate.getFutureCount();
  }

  /**
   * {@inheritDoc}
   */
  @JsonIgnore
  @Override
  public Future<Integer> getFutureRowCount() {
    return delegate.getFutureCount();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<DTO> getList() {
    return list;
  }

  /**
   * {@inheritDoc}
   */
  @JsonIgnore
  @Override
  public int getTotalCount() {
    return delegate.getTotalCount();
  }

  /**
   * {@inheritDoc}
   */
  @JsonIgnore
  @Override
  public int getTotalRowCount() {
    return delegate.getTotalCount();
  }

  /**
   * {@inheritDoc}
   */
  @JsonIgnore
  @Override
  public int getTotalPageCount() {
    return delegate.getTotalPageCount();
  }

  /**
   * {@inheritDoc}
   */
  @JsonIgnore
  @Override
  public int getPageSize() {
    return delegate.getPageSize();
  }

  /**
   * {@inheritDoc}
   */
  @JsonGetter
  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }

  /**
   * {@inheritDoc}
   */
  @JsonGetter
  @Override
  public boolean hasPrev() {
    return delegate.hasPrev();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDisplayXtoYofZ(String to, String of) {
    return delegate.getDisplayXtoYofZ(to, of);
  }
}
