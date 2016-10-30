package services.impl;

import static utils.Stopwatch.log;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSqlBuilder;

import models.ActionType;
import models.Locale;
import models.LogEntry;
import models.Message;
import models.Stat;
import play.Configuration;
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class LocaleServiceImpl extends AbstractModelService<Locale> implements LocaleService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceImpl.class);

	private final MessageService messageService;

	/**
	 * 
	 */
	@Inject
	public LocaleServiceImpl(Configuration configuration, MessageService messageService, LogEntryService logEntryService)
	{
		super(configuration, logEntryService);
		this.messageService = messageService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<UUID, Double> progress(List<UUID> localeIds, long keysSize)
	{
		List<Stat> stats = log(
			() -> Ebean
				.find(Stat.class)
				.setRawSql(
					RawSqlBuilder
						.parse("SELECT m.locale_id, count(m.id) FROM message m GROUP BY m.locale_id")
						.columnMapping("m.locale_id", "id")
						.columnMapping("count(m.id)", "count")
						.create())
				.where()
				.in("m.locale_id", localeIds)
				.findList(),
			LOGGER,
			"Retrieving locale progress");

		return stats.stream().collect(
			Collectors.groupingBy(k -> k.id, Collectors.averagingDouble(t -> (double)t.count / (double)keysSize)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preSave(Locale t, boolean update)
	{
		if(update)
			logEntryService.save(
				LogEntry.from(
					ActionType.Update,
					t.project,
					dto.Locale.class,
					dto.Locale.from(Locale.byId(t.id)),
					dto.Locale.from(t)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postSave(Locale t, boolean update)
	{
		if(!update)
			logEntryService.save(LogEntry.from(ActionType.Create, t.project, dto.Locale.class, null, dto.Locale.from(t)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preDelete(Locale t)
	{
		logEntryService.save(LogEntry.from(ActionType.Delete, t.project, dto.Locale.class, dto.Locale.from(t), null));

		messageService.delete(Message.byLocale(t.id));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preDelete(Collection<Locale> t)
	{
		logEntryService.save(t
			.stream()
			.map(l -> LogEntry.from(ActionType.Delete, l.project, dto.Locale.class, dto.Locale.from(l), null))
			.collect(Collectors.toList()));

		messageService.delete(Message.byLocales(t.stream().map(m -> m.id).collect(Collectors.toList())));
	}
}
