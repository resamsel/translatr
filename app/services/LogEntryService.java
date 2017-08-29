package services;

import com.google.inject.ImplementedBy;
import criterias.LogEntryCriteria;
import java.util.List;
import java.util.UUID;
import models.Aggregate;
import models.LogEntry;
import services.impl.LogEntryServiceImpl;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LogEntryServiceImpl.class)
public interface LogEntryService extends ModelService<LogEntry, UUID, LogEntryCriteria> {

  List<Aggregate> getAggregates(LogEntryCriteria criteria);

  int countBy(LogEntryCriteria criteria);
}
