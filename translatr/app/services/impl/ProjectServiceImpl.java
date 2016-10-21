package services.impl;

import static utils.Stopwatch.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.ActionType;
import models.LogEntry;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.ProjectService;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class ProjectServiceImpl extends AbstractModelService<Project> implements ProjectService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

	private final CacheApi cache;

	private final LocaleService localeService;

	private final KeyService keyService;

	/**
	 * 
	 */
	@Inject
	public ProjectServiceImpl(Configuration configuration, CacheApi cache, LocaleService localeService,
				KeyService keyService, LogEntryService logEntryService)
	{
		super(configuration, logEntryService);
		this.cache = cache;
		this.localeService = localeService;
		this.keyService = keyService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Project getById(UUID id)
	{
		return log(
			() -> cache.getOrElse(String.format("project:%s", id.toString()), () -> Project.byIdUncached(id), 60),
			LOGGER,
			"getById");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Project getByOwnerAndName(User user, String name)
	{
		return log(
			() -> cache.getOrElse(
				String.format("projectByOwnerAndName:%s:%s", user.id.toString(), name),
				() -> Project.byOwnerAndNameUncached(user, name),
				10 * 600),
			LOGGER,
			"byOwnerAndName");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preSave(Project t, boolean update)
	{
		if(update)
			logEntryService
				.save(LogEntry.from(ActionType.Update, t, dto.Project.class, toDto(Project.byId(t.id)), toDto(t)));
		if(t.owner == null)
			t.owner = User.loggedInUser();
		if(t.members == null)
			t.members = new ArrayList<>();
		if(t.members.isEmpty())
			t.members.add(new ProjectUser(ProjectRole.Owner).withProject(t).withUser(t.owner));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postSave(Project t, boolean update)
	{
		if(!update)
			logEntryService.save(LogEntry.from(ActionType.Create, t, dto.Project.class, null, toDto(t)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Project t)
	{
		keyService.delete(t.keys);
		localeService.delete(t.locales);

		logEntryService.save(LogEntry.from(ActionType.Delete, t, dto.Project.class, toDto(t), null));

		super.save(t.withName(String.format("%s-%s", t.id, t.name)).withDeleted(true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Collection<Project> t)
	{
		keyService.delete(t.stream().map(p -> p.keys).flatMap(k -> k.stream()).collect(Collectors.toList()));
		localeService.delete(t.stream().map(p -> p.locales).flatMap(l -> l.stream()).collect(Collectors.toList()));

		logEntryService.save(
			t.stream().map(p -> LogEntry.from(ActionType.Delete, p, dto.Project.class, toDto(p), null)).collect(
				Collectors.toList()));

		super.save(
			t.stream().map(p -> p.withName(String.format("%s-%s", p.id, p.name)).withDeleted(true)).collect(
				Collectors.toList()));
	}

	protected dto.Project toDto(Project t)
	{
		dto.Project out = dto.Project.from(t);

		out.keys = Collections.emptyList();
		out.locales = Collections.emptyList();
		out.messages = Collections.emptyList();

		return out;
	}
}
