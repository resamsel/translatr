package forms;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import play.Configuration;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Call;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 4 Sep 2016
 */
public class SearchForm {
  public String search;

  public Boolean missing;

  public Integer limit;

  public Integer offset = 0;

  public String order;

  public boolean hasMore = false;

  public String getSearch() {
    return search;
  }

  public void setSearch(String search) {
    this.search = search;
  }

  /**
   * @return the missing
   */
  public boolean isMissing() {
    return missing;
  }

  /**
   * @param missing the missing to set
   */
  public void setMissing(boolean missing) {
    this.missing = missing;
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

  public static Form<SearchForm> bindFromRequest(FormFactory formFactory,
      Configuration configuration) {
    Form<SearchForm> out = formFactory.form(SearchForm.class).bindFromRequest();

    SearchForm form = out.get();

    if (form.missing == null)
      form.missing = configuration.getBoolean("translatr.search.missing", false);
    if (form.limit == null)
      form.limit = configuration.getInt("translatr.search.limit", 20);
    if (form.order == null)
      form.order = "name";

    return out;
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
    try {
      return new URIBuilder(call.url())
          .addParameters(Arrays.asList(new BasicNameValuePair("limit", String.valueOf(limit)),
              new BasicNameValuePair("offset", String.valueOf(offset)),
              new BasicNameValuePair("search", search),
              new BasicNameValuePair("missing", String.valueOf(missing)),
              new BasicNameValuePair("order", order)))
          .toString();
    } catch (URISyntaxException e) {
      return call.url();
    }
  }

  public String urlWithOffset(Call call, int limit, int offset) {
    try {
      return new URIBuilder(call.url())
          .addParameters(Arrays.asList(new BasicNameValuePair("limit", String.valueOf(limit)),
              new BasicNameValuePair("offset", String.valueOf(offset)),
              new BasicNameValuePair("search", search),
              new BasicNameValuePair("missing", String.valueOf(missing)),
              new BasicNameValuePair("order", order)))
          .toString();
    } catch (URISyntaxException e) {
      return call.url();
    }
  }

  public String nextPage(Call call) {
    return urlWithOffset(call, limit, offset + limit);
  }

  public String previousPage(Call call) {
    return urlWithOffset(call, limit, Math.max(0, offset - limit));
  }

  /**
   * @param items
   */
  public void pager(List<?> items) {
    hasMore = items.size() > limit;
    if (hasMore)
      items.subList(limit, items.size()).clear();
  }
}
