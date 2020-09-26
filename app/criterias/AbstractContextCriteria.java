package criterias;

import javax.annotation.CheckForNull;
import java.util.UUID;

public abstract class AbstractContextCriteria<T extends AbstractContextCriteria<T>>
        extends AbstractFetchCriteria<T>
        implements ContextCriteria {
  private UUID loggedInUserId;

  @CheckForNull
  @Override
  public UUID getLoggedInUserId() {
    return loggedInUserId;
  }

  @Override
  public void setLoggedInUserId(@CheckForNull UUID loggedInUserId) {
    this.loggedInUserId = loggedInUserId;
  }

  public T withLoggedInUserId(UUID loggedInUserId) {
    setLoggedInUserId(loggedInUserId);
    return self;
  }
}
