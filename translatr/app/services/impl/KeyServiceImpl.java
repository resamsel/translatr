package services.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.avaje.ebean.Ebean;

import models.ActionType;
import models.Key;
import models.LogEntry;
import models.Message;
import services.KeyService;
import services.LogEntryService;
import services.MessageService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
public class KeyServiceImpl implements KeyService
{
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
		Ebean.delete(t);
	}
}
