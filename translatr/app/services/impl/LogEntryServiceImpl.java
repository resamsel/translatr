package services.impl;

import java.util.Collection;

import javax.inject.Singleton;

import com.avaje.ebean.Ebean;

import models.LogEntry;
import services.LogEntryService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class LogEntryServiceImpl implements LogEntryService
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogEntry save(LogEntry logEntry)
	{
		Ebean.save(logEntry);
		return logEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(Collection<LogEntry> t)
	{
		Ebean.save(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(LogEntry t)
	{
		Ebean.delete(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Collection<LogEntry> t)
	{
		Ebean.delete(t);
	}
}
