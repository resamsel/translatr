package criterias;

import com.avaje.ebean.ExpressionList;
import forms.SearchForm;

/**
 *
 * @author resamsel
 * @version 19 Oct 2016
 */
public class AccessTokenCriteria extends AbstractSearchCriteria<AccessTokenCriteria> {
  public static AccessTokenCriteria from(SearchForm form) {
    return new AccessTokenCriteria().with(form);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <U> ExpressionList<U> paged(ExpressionList<U> query) {
    query.order("whenCreated");

    return super.paged(query);
  }
}
