package repositories;

import com.google.inject.ImplementedBy;
import criterias.AccessTokenCriteria;
import criterias.LogEntryCriteria;
import java.util.UUID;
import models.AccessToken;
import models.LogEntry;
import repositories.impl.AccessTokenRepositoryImpl;
import repositories.impl.LogEntryRepositoryImpl;

@ImplementedBy(LogEntryRepositoryImpl.class)
public interface LogEntryRepository extends
    ModelRepository<LogEntry, UUID, LogEntryCriteria> {

  String[] PROPERTIES_TO_FETCH = {"user", "project"};

  int countBy(LogEntryCriteria criteria);
}
