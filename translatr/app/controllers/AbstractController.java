package controllers;

import java.util.UUID;

import com.feth.play.module.pa.PlayAuthenticate;

import commands.Command;
import models.Locale;
import models.Project;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Controller;
import services.UserService;
import utils.Template;

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

	protected final Injector injector;

	protected final CacheApi cache;

	protected final PlayAuthenticate auth;

	protected final UserService userService;

	/**
	 * @param injector
	 * @param userService
	 * @param auth
	 * 
	 */
	protected AbstractController(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService)
	{
		this.injector = injector;
		this.cache = cache;
		this.auth = auth;
		this.userService = userService;
	}

	/**
	 * @return
	 */
	protected Template createTemplate()
	{
		return Template.create(auth, User.loggedInUser());
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
