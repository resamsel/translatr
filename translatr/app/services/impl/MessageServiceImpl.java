package services.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.avaje.ebean.Ebean;

import models.ActionType;
import models.LogEntry;
import models.Message;
import services.LogEntryService;
import services.MessageService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class MessageServiceImpl implements MessageService
{
	private final LogEntryService logEntryService;

	/**
	 * 
	 */
	@Inject
	public MessageServiceImpl(LogEntryService logEntryService)
	{
		this.logEntryService = logEntryService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Message save(Message t)
	{
		boolean update = !Ebean.getBeanState(t).isNew();
		if(update)
			logEntryService.save(
				LogEntry.from(
					ActionType.Update,
					t.key.project,
					dto.Message.class,
					dto.Message.from(Message.byId(t.id)),
					dto.Message.from(t)));

		Ebean.save(t);
		Ebean.refresh(t);

		if(!update)
			logEntryService
				.save(LogEntry.from(ActionType.Create, t.key.project, dto.Message.class, null, dto.Message.from(t)));

		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Message t)
	{
		logEntryService
			.save(LogEntry.from(ActionType.Delete, t.key.project, dto.Message.class, dto.Message.from(t), null));

		Ebean.delete(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Collection<Message> t)
	{
		logEntryService.save(t
			.stream()
			.map(m -> LogEntry.from(ActionType.Delete, m.key.project, dto.Message.class, dto.Message.from(m), null))
			.collect(Collectors.toList()));

		Ebean.delete(t);
	}
}
