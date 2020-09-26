package criterias;

import play.mvc.Http;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGetCriteria<T extends AbstractGetCriteria<T, U>, U>
        extends AbstractContextCriteria<T> implements GetCriteria<U> {

  private final U id;
  private final Http.Request request;

  protected AbstractGetCriteria(U id, Http.Request request) {
    this.id = id;
    this.request = requireNonNull(request);
  }

  @Nonnull
  @Override
  public U getId() {
    return id;
  }

  @Override
  public Http.Request getRequest() {
    return request;
  }
}
