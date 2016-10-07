package controllers;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import commands.Command;
import exporters.Exporter;
import exporters.GettextExporter;
import exporters.JavaPropertiesExporter;
import exporters.PlayMessagesExporter;
import models.FileType;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import scala.collection.JavaConversions;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;
import services.UserService;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 */
@With(ContextAction.class)
public class Application extends AbstractController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static final String USER_ROLE = "user";

	public static final String FLASH_MESSAGE_KEY = "message";

	public static final String FLASH_ERROR_KEY = "error";

	private final ProjectService projectService;

	private final LocaleService localeService;

	private final KeyService keyService;

	private final MessageService messageService;

	@Inject
	public Application(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
				ProjectService projectService, LocaleService localeService, KeyService keyService,
				MessageService messageService)
	{
		super(injector, cache, auth, userService);

		this.projectService = projectService;
		this.localeService = localeService;
		this.keyService = keyService;
		this.messageService = messageService;
	}

	public Result index()
	{
		return ok(views.html.index.render(createTemplate()));
	}

	public Result login()
	{
		return ok(views.html.login.render(createTemplate()));
	}

	public Result oAuthDenied(final String providerKey)
	{
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		flash(FLASH_ERROR_KEY, "You need to accept the OAuth connection in order to use this website!");
		return redirect(routes.Application.index());
	}

	public Result download(UUID id, String fileType)
	{
		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		Exporter exporter;
		switch(FileType.fromKey(fileType))
		{
			case PlayMessages:
				exporter = new PlayMessagesExporter();
			break;
			case JavaProperties:
				exporter = new JavaPropertiesExporter();
			break;
			case Gettext:
				exporter = new GettextExporter();
			break;
			default:
				return badRequest("File type " + fileType + " not supported yet");
		}

		exporter.addHeaders(response(), locale);

		return ok(new ByteArrayInputStream(exporter.apply(locale)));
	}

	public Result load()
	{
		String brand = ctx().messages().at("brand");
		Project project = Project.byOwnerAndName(User.byUsername("translatr"), brand);
		if(project == null)
		{
			project = projectService.save(new Project(brand));
		}
		else if(project.deleted)
		{
			project.deleted = false;
			projectService.save(project);
		}

		select(project);

		for(Entry<String, scala.collection.immutable.Map<String, String>> bundle : JavaConversions
			.mapAsJavaMap(ctx().messages().messagesApi().scalaApi().messages())
			.entrySet())
		{
			LOGGER.debug("Key: {}", bundle.getKey());
			if("default.play".equals(bundle.getKey()))
				break;
			Locale locale = Locale.byProjectAndName(project, bundle.getKey());
			if(locale == null)
				locale = localeService.save(new Locale(project, bundle.getKey()));
			for(Entry<String, String> msg : JavaConversions.mapAsJavaMap(bundle.getValue()).entrySet())
			{
				Key key = Key.byProjectAndName(project, msg.getKey());
				if(key == null)
					key = keyService.save(new Key(project, msg.getKey()));
				Message message = Message.byKeyAndLocale(key, locale);
				if(message == null)
					messageService.save(new Message(locale, key, msg.getValue()));
			}
		}

		return redirect(routes.Projects.project(project.id));
	}

	public Result commandExecute(String commandKey)
	{
		Command<?> command = getCommand(commandKey);

		if(command == null)
			notFound(Json.toJson("Command not found"));

		command.execute();

		Call call = command.redirect();

		if(call != null)
			return redirect(call);

		String referer = request().getHeader("Referer");

		if(referer == null)
			return redirect(routes.Application.index());

		return redirect(referer);
	}

	public Result javascriptRoutes()
	{
		return ok(
			JavaScriptReverseRouter.create(
				"jsRoutes",
				routes.javascript.Dashboards.search(),
				routes.javascript.Projects.search(),
				routes.javascript.Projects.keysSearch(),
				routes.javascript.Locales.keysSearch(),
				routes.javascript.Locales.locale(),
				routes.javascript.Keys.key(),
				routes.javascript.Keys.createImmediately(),
				routes.javascript.Keys.remove(),
				routes.javascript.Api.getMessage(),
				routes.javascript.Api.putMessage(),
				routes.javascript.Api.findMessages()));
	}
}
