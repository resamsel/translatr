package services.impl;

import static utils.Stopwatch.log;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSqlBuilder;

import models.ActionType;
import models.Key;
import models.LogEntry;
import models.Message;
import models.Stat;
import services.KeyService;
import services.LogEntryService;
import services.MessageService;
import utils.TransactionUtils;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
public class KeyServiceImpl implements KeyService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceImpl.class);

	private final MessageService messageService;

	private final LogEntryService logEntryService;

	/**
	 * 
	 */
	@Inject
	public KeyServiceImpl(MessageService messageService, LogEntryService logEntryService)
	{
		this.messageService = messageService;
		this.logEntryService = logEntryService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<UUID, Double> progress(List<UUID> keyIds, long localesSize)
	{
		List<Stat> stats = log(
			() -> Ebean
				.find(Stat.class)
				.setRawSql(
					RawSqlBuilder
						.parse("SELECT m.key_id, count(m.id) FROM message m GROUP BY m.key_id")
						.columnMapping("m.key_id", "id")
						.columnMapping("count(m.id)", "count")
						.create())
				.where()
				.in("m.key_id", keyIds)
				.findList(),
			LOGGER,
			"Retrieving key progress");

		return stats.stream().collect(
			Collectors.groupingBy(k -> k.id, Collectors.averagingDouble(t -> (double)t.count / (double)localesSize)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Key save(Key t)
	{
		boolean update = !Ebean.getBeanState(t).isNew();
		if(update)
			logEntryService.save(
				LogEntry.from(ActionType.Update, t.project, dto.Key.class, dto.Key.from(Key.byId(t.id)), dto.Key.from(t)));

		Ebean.save(t);
		Ebean.refresh(t);

		if(!update)
			logEntryService.save(LogEntry.from(ActionType.Create, t.project, dto.Key.class, null, dto.Key.from(t)));

		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Key t)
	{
		logEntryService.save(LogEntry.from(ActionType.Delete, t.project, dto.Key.class, dto.Key.from(t), null));

		messageService.delete(Message.byKey(t));
		Ebean.delete(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Collection<Key> t)
	{
		messageService.delete(Message.byKeys(t.stream().map(k -> k.id).collect(Collectors.toList())));

		try
		{
			TransactionUtils.batchExecute((tx) -> {
				Ebean.delete(t);
			});
		}
		catch(Exception e)
		{
			LOGGER.error("Error while batch deleting keys", e);
		}
	}
}
