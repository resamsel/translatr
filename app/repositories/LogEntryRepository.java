package repositories;

import com.google.inject.ImplementedBy;
import criterias.LogEntryCriteria;
import models.LogEntry;
import repositories.impl.LogEntryRepositoryImpl;

import java.util.UUID;

@ImplementedBy(LogEntryRepositoryImpl.class)
public interface LogEntryRepository extends
    ModelRepository<LogEntry, UUID, LogEntryCriteria> {

  String[] PROPERTIES_TO_FETCH = {"user", "project"};

  int countBy(LogEntryCriteria criteria);
}
