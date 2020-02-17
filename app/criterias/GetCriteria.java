package criterias;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.UUID;

public interface GetCriteria<T> extends FetchCriteria {
  @Nonnull
  T getId();

  @CheckForNull
  UUID getLoggedInUserId();
}
