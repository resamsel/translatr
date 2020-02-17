package criterias;

import java.util.List;
import java.util.UUID;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public interface SearchCriteria extends FetchCriteria {
  UUID getLoggedInUserId();

  Integer getLimit();

  String getOrder();

  Integer getOffset();

  String getSearch();
}
