package criterias;

import javax.annotation.Nonnull;

public interface GetCriteria<T> extends ContextCriteria {
  @Nonnull
  T getId();

  static <T> GetCriteria<T> of(T id) {
    return new DefaultGetCriteria<>(id);
  }
}
