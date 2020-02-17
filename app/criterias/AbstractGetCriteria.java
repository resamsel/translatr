package criterias;

import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckForNull;
import java.util.UUID;

public abstract class AbstractGetCriteria<T extends AbstractGetCriteria<T, U>, U>
    extends AbstractFetchCriteria<T> implements GetCriteria<U> {

  private UUID loggedInUserId;
  private final U id;

  protected AbstractGetCriteria(U id) {
    this.id = id;
  }

  @NotNull
  @Override
  public U getId() {
    return id;
  }

  @CheckForNull
  @Override
  public UUID getLoggedInUserId() {
    return loggedInUserId;
  }

  public void setLoggedInUserId(UUID loggedInUserId) {
    this.loggedInUserId = loggedInUserId;
  }

  public T withLoggedInUserId(UUID loggedInUserId) {
    setLoggedInUserId(loggedInUserId);
    return self;
  }
}
