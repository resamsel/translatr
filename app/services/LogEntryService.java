package services;

import io.ebean.PagedList;
import com.google.inject.ImplementedBy;
import criterias.LogEntryCriteria;
import models.Aggregate;
import models.LogEntry;
import services.impl.LogEntryServiceImpl;

import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LogEntryServiceImpl.class)
public interface LogEntryService extends ModelService<LogEntry, UUID, LogEntryCriteria> {

  PagedList<Aggregate> getAggregates(LogEntryCriteria criteria);

  int countBy(LogEntryCriteria criteria);
}
