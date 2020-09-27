package criterias;

import play.mvc.Http;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public abstract class AbstractGetCriteria<T extends AbstractGetCriteria<T, U>, U>
        extends AbstractContextCriteria<T> implements GetCriteria<U> {

  private final U id;
  private final Http.Request request;

  protected AbstractGetCriteria(U id, Http.Request request) {
    this.id = id;
    this.request = request;
  }

  @Nonnull
  @Override
  public U getId() {
    return id;
  }

  @CheckForNull
  @Override
  public Http.Request getRequest() {
    return request;
  }
}
