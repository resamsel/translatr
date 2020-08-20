package dto;

import io.ebean.PagedList;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import criterias.HasNextPagedList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class DtoPagedList<MODEL, DTO extends Dto> extends Dto implements PagedList<DTO> {
  private static final long serialVersionUID = -702426798092435389L;

  private final List<DTO> list;

  @JsonIgnore
  private final PagedList<MODEL> delegate;

  private int offset;
  private final int limit;
  private final int total;

  /**
   *
   */
  public DtoPagedList(PagedList<MODEL> delegate, Function<MODEL, DTO> mapper) {
    this.delegate = delegate;
    this.list = delegate.getList()
        .stream()
        .map(mapper)
        .collect(toList());

    if (delegate instanceof HasNextPagedList)
      this.offset = ((HasNextPagedList<?>) delegate).getOffset();
    this.limit = delegate.getPageSize();
    this.total = delegate.getTotalCount();
  }

  public int getOffset() {
    return offset;
  }

  public int getLimit() {
    return limit;
  }

  public int getTotal() {
    return total;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadCount() {
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @JsonIgnore
  @Override
  public Future<Integer> getFutureCount() {
    return delegate.getFutureCount();
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
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

  @Override
  public int getPageIndex() {
    return delegate.getPageIndex();
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
