package services;

import java.util.List;

import com.google.inject.ImplementedBy;

import criterias.LogEntryCriteria;
import models.Aggregate;
import models.LogEntry;
import services.impl.LogEntryServiceImpl;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LogEntryServiceImpl.class)
public interface LogEntryService extends ModelService<LogEntry>
{
	List<Aggregate> getStats(LogEntryCriteria criteria);

	/**
	 * @param criteria
	 * @return
	 */
	List<Aggregate> getAggregates(LogEntryCriteria criteria);
}
