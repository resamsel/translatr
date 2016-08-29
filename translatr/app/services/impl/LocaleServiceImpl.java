package services.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.avaje.ebean.Ebean;

import models.ActionType;
import models.Locale;
import models.LogEntry;
import models.Message;
import services.LocaleService;
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
public class LocaleServiceImpl implements LocaleService
{
	private final LogEntryService logEntryService;

	private final MessageService messageService;

	/**
	 * 
	 */
	@Inject
	public LocaleServiceImpl(MessageService messageService, LogEntryService logEntryService)
	{
		this.messageService = messageService;
		this.logEntryService = logEntryService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Locale save(Locale t)
	{
		boolean update = !Ebean.getBeanState(t).isNew();
		if(update)
			logEntryService.save(
				LogEntry.from(
					ActionType.Update,
					t.project,
					dto.Locale.class,
					dto.Locale.from(Locale.byId(t.id)),
					dto.Locale.from(t)));

		Ebean.save(t);
		Ebean.refresh(t);

		if(!update)
			logEntryService.save(LogEntry.from(ActionType.Create, t.project, dto.Locale.class, null, dto.Locale.from(t)));

		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Locale t)
	{
		logEntryService.save(LogEntry.from(ActionType.Delete, t.project, dto.Locale.class, dto.Locale.from(t), null));

		messageService.delete(Message.byLocale(t.id));
		Ebean.delete(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Collection<Locale> t)
	{
		messageService.delete(Message.byLocales(t.stream().map(m -> m.id).collect(Collectors.toList())));
		Ebean.delete(t);
	}
}
