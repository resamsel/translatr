package criterias;

import javax.annotation.Nonnull;

public abstract class AbstractGetCriteria<T extends AbstractGetCriteria<T, U>, U>
        extends AbstractContextCriteria<T>
        implements GetCriteria<U> {

  private final U id;

  protected AbstractGetCriteria(U id) {
    this.id = id;
  }

  @Nonnull
  @Override
  public U getId() {
    return id;
  }
}
