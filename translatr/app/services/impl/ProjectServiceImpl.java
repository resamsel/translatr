package services.impl;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.avaje.ebean.Ebean;

import models.ActionType;
import models.LogEntry;
import models.Project;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.ProjectService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class ProjectServiceImpl implements ProjectService
{
	private final LocaleService localeService;

	private final KeyService keyService;

	private final LogEntryService logEntryService;

	/**
	 * 
	 */
	@Inject
	public ProjectServiceImpl(LocaleService localeService, KeyService keyService, LogEntryService logEntryService)
	{
		this.localeService = localeService;
		this.keyService = keyService;
		this.logEntryService = logEntryService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Project save(Project t)
	{
		boolean update = !Ebean.getBeanState(t).isNew();
		if(update)
			logEntryService.save(
				LogEntry.from(
					ActionType.Update,
					t,
					dto.Project.class,
					dto.Project.from(Project.byId(t.id)),
					dto.Project.from(t)));

		Ebean.save(t);
		Ebean.refresh(t);

		if(!update)
			logEntryService.save(LogEntry.from(ActionType.Create, t, dto.Project.class, null, dto.Project.from(t)));

		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Project t)
	{
		logEntryService.save(LogEntry.from(ActionType.Delete, t, dto.Project.class, dto.Project.from(t), null));

		keyService.delete(t.keys);
		localeService.delete(t.locales);

		Ebean.save(t.withDeleted(true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Collection<Project> t)
	{
		keyService.delete(t.stream().map(p -> p.keys).flatMap(k -> k.stream()).collect(Collectors.toList()));
		localeService.delete(t.stream().map(p -> p.locales).flatMap(l -> l.stream()).collect(Collectors.toList()));

		t.stream().forEach(
			p -> logEntryService.save(LogEntry.from(ActionType.Delete, p, dto.Project.class, dto.Project.from(p), null)));

		Ebean.delete(t);
	}
}
