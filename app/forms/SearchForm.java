package forms;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import play.mvc.Call;

/**
 * @author resamsel
 * @version 4 Sep 2016
 */
public class SearchForm {

  public String search;

  public Integer limit;

  public Integer offset = 0;

  public String order;

  @JsonIgnore
  public boolean hasMore = false;

  public String getSearch() {
    return search;
  }

  public void setSearch(String search) {
    this.search = search;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  @Override
  public String toString() {
    return String.format("SearchForm [search=%s, limit=%s, offset=%s]", search, limit, offset);
  }

  /**
   * @return the hasMore
   */
  public boolean isHasMore() {
    return hasMore;
  }

  /**
   * @param hasMore the hasMore to set
   */
  public void setHasMore(boolean hasMore) {
    this.hasMore = hasMore;
  }

  /**
   * @return the order
   */
  public String getOrder() {
    return order;
  }

  /**
   * @param order the order to set
   */
  public void setOrder(String order) {
    this.order = order;
  }

  public String urlWithOrder(Call call, String order) {
    return urlWith(call, limit, offset, order, search);
  }

  public String urlWithOffset(Call call, int limit, int offset) {
    return urlWith(call, limit, offset, order, search);
  }

  public String nextPage(Call call) {
    return urlWithOffset(call, limit, offset + limit);
  }

  public String previousPage(Call call) {
    return urlWithOffset(call, limit, Math.max(0, offset - limit));
  }

  /**
   * @param pagedList
   */
  public <T> PagedList<T> pager(PagedList<T> pagedList) {
    hasMore = Objects.requireNonNull(pagedList.hasNext(), "pagedList is null");
    return pagedList;
  }

  protected List<NameValuePair> parameters(int limit, int offset, String order, String search) {
    List<NameValuePair> parameters =
        new ArrayList<>(Arrays.asList(new BasicNameValuePair("limit", String.valueOf(limit)),
            new BasicNameValuePair("offset", String.valueOf(offset)),
            new BasicNameValuePair("order", order)));

    if (search != null && search.length() > 0) {
      parameters.add(new BasicNameValuePair("search", search));
    }

    return parameters;
  }

  public String urlWith(Call call, int limit, int offset, String order, String search) {
    try {
      return new URIBuilder(call.url()).addParameters(parameters(limit, offset, order, search))
          .toString();
    } catch (URISyntaxException e) {
      return call.url();
    }
  }

  public SearchForm update(String search, String order, int limit, int offset) {
    this.search = search;
    this.order = order;
    this.limit = limit;
    this.offset = offset;

    return this;
  }
}
