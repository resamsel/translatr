package controllers;

import java.util.UUID;

import commands.Command;
import models.Locale;
import models.Project;
import play.cache.CacheApi;
import play.mvc.Controller;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
public abstract class AbstractController extends Controller
{
	private static final String COMMAND_FORMAT = "command:%s";

	private final CacheApi cache;

	/**
	 * 
	 */
	protected AbstractController(CacheApi cache)
	{
		this.cache = cache;
	}

	protected void select(Project project)
	{
		// session("projectId", project.id.toString());
		ctx().args.put("projectId", project.id);
	}

	protected void select(Locale locale)
	{
		// session("localeId", locale.id.toString());
		// session("localeName", locale.name);
	}

	protected Command<?> getCommand(String key)
	{
		return cache.get(key);
	}

	/**
	 * @param command
	 */
	protected String undoCommand(Command<?> command)
	{
		String undoKey = String.format(COMMAND_FORMAT, UUID.randomUUID());

		cache.set(undoKey, command, 120);

		flash("undo", undoKey);

		return undoKey;
	}
}
