package criterias;

import play.mvc.Http;

public class DefaultGetCriteria<T extends DefaultGetCriteria<T, U>, U>
        extends AbstractGetCriteria<DefaultGetCriteria<T, U>, U> {
  public DefaultGetCriteria(U id, Http.Request request, String... fetches) {
    super(id, request);

    withFetches(fetches);
  }
}
