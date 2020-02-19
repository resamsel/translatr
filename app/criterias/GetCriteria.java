package criterias;

import javax.annotation.Nonnull;

public interface GetCriteria<T> extends ContextCriteria {
  @Nonnull
  T getId();
}
