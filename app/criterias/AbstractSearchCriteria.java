package criterias;

import io.ebean.ExpressionList;
import forms.SearchForm;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Http.Request;
import utils.NumberUtils;

import javax.annotation.Nonnull;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * @author resamsel
 * @version 31 Aug 2016
 */
public abstract class AbstractSearchCriteria<T extends AbstractSearchCriteria<T>>
    extends AbstractFetchCriteria<T> implements SearchCriteria {

  protected final String type;

  private Request request;

  private Integer offset;

  private Integer limit;

  private String order;

  private String search;

  private UUID userId;

  private UUID loggedInUserId;

  public AbstractSearchCriteria(String type) {
    this.type = type;
  }

  public Request getRequest() {
    return request;
  }

  public void setRequest(@Nonnull Request request) {
    this.request = requireNonNull(request);
  }

  public T withRequest(@Nonnull Request request) {
    setRequest(request);
    return self;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public T withUserId(UUID userId) {
    setUserId(userId);
    return self;
  }

  public UUID getLoggedInUserId() {
    return loggedInUserId;
  }

  public void setLoggedInUserId(UUID loggedInUserId) {
    this.loggedInUserId = loggedInUserId;
  }

  public T withLoggedInUserId(UUID loggedInUserId) {
    setLoggedInUserId(loggedInUserId);
    return self;
  }

  /**
   * @return the limit
   */
  @Override
  public Integer getLimit() {
    return limit;
  }

  /**
   * @param limit the limit to set
   */
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public T withLimit(Integer limit) {
    setLimit(limit);
    return self;
  }

  /**
   * @return the order
   */
  @Override
  public String getOrder() {
    return order;
  }

  /**
   * @param order the order to set
   */
  public void setOrder(String order) {
    this.order = order;
  }

  public T withOrder(String order) {
    setOrder(order);
    return self;
  }

  /**
   * @return the offset
   */
  @Override
  public Integer getOffset() {
    return offset;
  }

  /**
   * @param offset the offset to set
   */
  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public T withOffset(Integer offset) {
    setOffset(offset);
    return self;
  }

  /**
   * @return the search
   */
  @Override
  public String getSearch() {
    return search;
  }

  /**
   * @param search the search to set
   */
  public void setSearch(String search) {
    this.search = search;
  }

  public T withSearch(String search) {
    setSearch(search);
    return self;
  }

  public T with(SearchForm form) {
    return self.withSearch(form.search).withOffset(form.offset).withLimit(form.limit)
        .withOrder(form.order);
  }

  public T with(@Nonnull Request request) {
    return self
        .withRequest(requireNonNull(request))
        .withSearch(request.getQueryString("search"))
        .withOffset(NumberUtils.parseInt(request.getQueryString("offset")))
        .withLimit(NumberUtils.parseInt(request.getQueryString("limit")))
        .withOrder(request.getQueryString("order"))
        .withFetches(StringUtils.split(request.getQueryString("fetch"), ","));
  }

  public <U> ExpressionList<U> paged(ExpressionList<U> query) {
    if (getLimit() != null) {
      query.setMaxRows(getLimit());
    }

    if (getOffset() != null) {
      query.setFirstRow(getOffset());
    }

    if (getOrder() != null) {
      query.order(getOrder());
    }

    return query;
  }

  public String getCacheKey() {
    return String.format(
        "%s:criteria:%s:%s:%s:%d:%d:%s",
        type, userId, search, order, limit, offset, getCacheKeyParticle()
    );
  }

  /**
   * Must be overridden by subclasses to allow caching.
   */
  protected abstract String getCacheKeyParticle();
}
