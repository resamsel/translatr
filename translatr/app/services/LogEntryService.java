package services;

import java.util.Collection;

import com.google.inject.ImplementedBy;

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
	void save(Collection<LogEntry> t);
}
