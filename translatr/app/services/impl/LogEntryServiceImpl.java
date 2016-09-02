package services.impl;

import java.util.Collection;
import java.util.UUID;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import models.LogEntry;
import models.Project;
import play.mvc.Http.Context;
import services.LogEntryService;
import utils.TransactionUtils;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryServiceImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LogEntry save(LogEntry logEntry)
	{
		if(logEntry.project == null)
		{
			if(Context.current().args.containsKey("projectId"))
			{
				logEntry.project = new Project();
				logEntry.project.id = (UUID)Context.current().args.get("projectId");
			}
			else
			{
				LOGGER.warn("Project has not been set and was not found in context");
				return null;
			}
		}

		Ebean.save(logEntry);
		return logEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(Collection<LogEntry> t)
	{
		try
		{
			TransactionUtils.batchExecute((tx) -> {
				Ebean.save(t);
			});
		}
		catch(Exception e)
		{
			LOGGER.error("Error while batch saving log entries", e);
		}
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
		try
		{
			TransactionUtils.batchExecute((tx) -> {
				Ebean.delete(t);
			});
		}
		catch(Exception e)
		{
			LOGGER.error("Error while batch deleting log entries", e);
		}
	}
}
