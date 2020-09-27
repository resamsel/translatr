package criterias;

import play.mvc.Http;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface GetCriteria<U> extends ContextCriteria {
  @Nonnull
  U getId();

  @CheckForNull
  Http.Request getRequest();

  static <T extends DefaultGetCriteria<T, U>, U> DefaultGetCriteria<T, U> from(U id, Http.Request request, String... fetches) {
    return new DefaultGetCriteria<T, U>(id, request, fetches);
  }
}
