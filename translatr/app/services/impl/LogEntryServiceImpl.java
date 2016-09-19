package services.impl;

import static utils.Stopwatch.log;

import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSqlBuilder;

import criterias.LogEntryCriteria;
import models.Aggregate;
import models.LogEntry;
import models.Project;
import play.mvc.Http.Context;
import services.LogEntryService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class LogEntryServiceImpl extends AbstractModelService<LogEntry> implements LogEntryService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryServiceImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Aggregate> getStats(LogEntryCriteria criteria)
	{
		return log(
			() -> Ebean
				.find(Aggregate.class)
				.setRawSql(
					RawSqlBuilder
						.parse(
							"select extract(epoch from date_trunc('hour', when_created))*1000 as millis, count(*) as cnt from log_entry group by 1 order by 1")
						.columnMapping("extract(epoch from date_trunc('hour', when_created))*1000", "millis")
						.columnMapping("count(*)", "value")
						.create())
				.where()
				.eq("project_id", criteria.getProjectId())
				.findList(),
			LOGGER,
			"Retrieving log entry stats");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preSave(LogEntry logEntry, boolean update)
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
			}
		}
	}
}
