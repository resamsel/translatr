package controllers;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;

import actions.ContextAction;
import commands.Command;
import commands.RevertDeleteKeyCommand;
import criterias.MessageCriteria;
import forms.KeyForm;
import forms.SearchForm;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
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

	private final FormFactory formFactory;

	private final ProjectService projectService;

	private final LocaleService localeService;

	private final KeyService keyService;

	private final MessageService messageService;

	private final Configuration configuration;

	@Inject
	public Application(Injector injector, CacheApi cache, FormFactory formFactory, PlayAuthenticate auth,
				UserService userService, ProjectService projectService, LocaleService localeService, KeyService keyService,
				MessageService messageService, Configuration configuration)
	{
		super(injector, cache, auth, userService);

		this.formFactory = formFactory;
		this.projectService = projectService;
		this.localeService = localeService;
		this.keyService = keyService;
		this.messageService = messageService;
		this.configuration = configuration;
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

	public Result key(UUID id)
	{
		Key key = Key.byId(id);

		if(key == null)
			return redirect(routes.Application.index());

		select(key.project);

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);

		Collections.sort(key.project.keys, (a, b) -> a.name.compareTo(b.name));

		List<Locale> locales = Locale.byProject(key.project);
		Map<UUID, Message> messages = Message.findBy(new MessageCriteria().withKeyName(key.name)).stream().collect(
			groupingBy(m -> m.locale.id, Collectors.reducing(null, a -> a, (a, b) -> b)));

		return ok(views.html.keys.key.render(createTemplate(), key, locales, messages, form));
	}

	public Result keyCreate(UUID projectId, UUID localeId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<KeyForm> form = formFactory.form(KeyForm.class).bindFromRequest();

		if(form.hasErrors())
			return badRequest(views.html.keys.create.render(createTemplate(), project, form));

		Key key = form.get().into(new Key());

		key.project = project;

		LOGGER.debug("Key: {}", Json.toJson(key));

		keyService.save(key);

		if(localeId != null)
		{
			Locale locale = Locale.byId(localeId);

			return redirect(routes.Locales.locale(locale.id).withFragment("#key=" + key.name));
		}

		return redirect(routes.Application.key(key.id));
	}

	public Result keyCreateImmediately(UUID projectId, String keyName)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		if(keyName.length() > Key.NAME_LENGTH)
			return badRequest(
				views.html.keys.create.render(
					createTemplate(),
					project,
					formFactory.form(KeyForm.class).bind(ImmutableMap.of("name", keyName))));

		Key key = Key.byProjectAndName(project, keyName);

		if(key == null)
		{
			key = new Key(project, keyName);

			LOGGER.debug("Key: {}", Json.toJson(key));

			keyService.save(key);
		}

		return redirect(routes.Application.key(key.id));
	}

	public Result keyEdit(UUID keyId)
	{
		Key key = Key.byId(keyId);

		if(key == null)
			return redirect(routes.Application.index());

		select(key.project);

		if("POST".equals(request().method()))
		{
			Form<KeyForm> form = formFactory.form(KeyForm.class).bindFromRequest();

			if(form.hasErrors())
				return badRequest(views.html.keys.edit.render(createTemplate(), key, form));

			keyService.save(form.get().into(key));

			return redirect(routes.Projects.keys(key.project.id));
		}

		return ok(
			views.html.keys.edit.render(createTemplate(), key, formFactory.form(KeyForm.class).fill(KeyForm.from(key))));
	}

	public Result keyRemove(UUID keyId, UUID localeId)
	{
		Key key = Key.byId(keyId);

		LOGGER.debug("Key: {}", Json.toJson(key));

		if(key == null)
			return redirect(routes.Application.index());

		select(key.project);

		undoCommand(injector.instanceOf(RevertDeleteKeyCommand.class).with(key));

		keyService.delete(key);

		if(localeId != null)
		{
			Locale locale = Locale.byId(localeId);
			if(locale != null)
				return redirect(routes.Locales.locale(locale.id));
		}

		LOGGER.debug("Go to projectKeys: {}", Json.toJson(key));

		return redirect(routes.Projects.keys(key.project.id));
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
				routes.javascript.Application.key(),
				routes.javascript.Application.keyCreateImmediately(),
				routes.javascript.Application.keyRemove(),
				routes.javascript.Api.getMessage(),
				routes.javascript.Api.putMessage(),
				routes.javascript.Api.findMessages()));
	}
}
