package criterias;

import javax.annotation.CheckForNull;
import java.util.UUID;

public interface ContextCriteria extends FetchCriteria {

  @CheckForNull
  UUID getLoggedInUserId();
}
