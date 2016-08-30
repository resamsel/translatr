package controllers;

import static utils.FormatUtils.formatLocale;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.ContextAction;
import commands.Command;
import commands.RevertDeleteKeyCommand;
import commands.RevertDeleteLocaleCommand;
import commands.RevertDeleteProjectCommand;
import exporters.Exporter;
import exporters.JavaPropertiesExporter;
import exporters.PlayMessagesExporter;
import forms.ImportLocaleForm;
import importers.Importer;
import importers.JavaPropertiesImporter;
import importers.PlayMessagesImporter;
import models.FileType;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import play.cache.CacheApi;
import play.data.FormFactory;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import scala.collection.JavaConversions;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 */
@With(ContextAction.class)
public class Application extends Controller
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private static final String COMMAND_FORMAT = "command:%s";

	private final Injector injector;

	private final FormFactory formFactory;

	private final CacheApi cache;

	private final ProjectService projectService;

	private final LocaleService localeService;

	private final KeyService keyService;

	private final MessageService messageService;

	@Inject
	public Application(Injector injector, FormFactory formFactory, CacheApi cache, ProjectService projectService,
				LocaleService localeService, KeyService keyService, MessageService messageService)
	{
		this.injector = injector;
		this.formFactory = formFactory;
		this.cache = cache;
		this.projectService = projectService;
		this.localeService = localeService;
		this.keyService = keyService;
		this.messageService = messageService;
	}

	private void select(Project project)
	{
		// session("projectId", project.id.toString());
	}

	private void select(Locale locale)
	{
		// session("localeId", locale.id.toString());
		// session("localeName", locale.name);
	}

	public Result index()
	{
		// return ok(views.html.index.render());
		return redirect(routes.Application.dashboard());
	}

	public Result dashboard()
	{
		return ok(views.html.dashboard.render(Project.all()));
	}

	public Result project(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		return ok(views.html.project.render(project));
	}

	public Result projectCreate()
	{
		Project form = formFactory.form(Project.class).bindFromRequest().get();

		LOGGER.debug("Project: {}", Json.toJson(form));

		Project project = Project.byName(form.name);
		if(project != null)
			project.updateFrom(form).withDeleted(false);
		else
			project = form;
		projectService.save(project);

		return redirect(routes.Application.project(project.id));
	}

	public Result projectEdit(UUID projectId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		if("POST".equals(request().method()))
		{
			Project changed = formFactory.form(Project.class).bindFromRequest().get();

			project.name = changed.name;

			projectService.save(project);

			return redirect(routes.Application.project(project.id));
		}

		return ok(views.html.projectEdit.render(project));
	}

	public Result projectRemove(UUID projectId)
	{
		Project project = Project.byId(projectId);

		LOGGER.debug("Key: {}", Json.toJson(project));

		if(project == null)
			return redirect(routes.Application.index());

		undoCommand(new RevertDeleteProjectCommand(project));

		projectService.delete(project);

		return redirect(routes.Application.dashboard());
	}

	public Result projectLocales(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		java.util.Locale locale = ctx().lang().locale();
		Collections.sort(project.locales, (a, b) -> formatLocale(locale, a).compareTo(formatLocale(locale, b)));

		return ok(views.html.projectLocales.render(project, project.locales));
	}

	public Result projectKeys(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Collections.sort(project.keys, (a, b) -> a.name.compareTo(b.name));

		return ok(views.html.projectKeys.render(project, project.keys));
	}

	public Result locale(UUID id)
	{
		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		Collections.sort(locale.project.keys, (a, b) -> a.name.compareTo(b.name));

		List<Locale> locales = Locale.byProject(locale.project);

		return ok(views.html.locale.render(locale.project, locale, locales));
	}

	public Result localeImport(UUID id)
	{
		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		if("POST".equals(request().method()))
		{
			importLocale(locale, request());

			return redirect(routes.Application.locale(id));
		}
		else
		{
			return ok(views.html.localeImport.render(locale.project, locale));
		}
	}

	public Result localeExport(UUID id, String fileType)
	{
		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		Exporter exporter;
		switch(FileType.fromKey(fileType))
		{
			case PlayMessages:
				exporter = new PlayMessagesExporter();
			break;
			case JavaProperties:
				exporter = new JavaPropertiesExporter();
			break;
			default:
				return badRequest("File type " + fileType + " not supported yet");
		}

		exporter.addHeaders(response(), locale);

		return ok(new ByteArrayInputStream(exporter.apply(locale)));
	}

	public Result localeCreate(UUID projectId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		Locale locale = formFactory.form(Locale.class).bindFromRequest().get();

		locale.project = project;

		localeService.save(locale);

		importLocale(locale, request());

		return redirect(routes.Application.locale(locale.id));
	}

	public Result localeEdit(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		if("POST".equals(request().method()))
		{
			Locale changed = formFactory.form(Locale.class).bindFromRequest().get();

			locale.name = changed.name;

			localeService.save(locale);

			return redirect(routes.Application.projectLocales(locale.project.id));
		}

		return ok(views.html.localeEdit.render(locale));
	}

	public Result localeRemove(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		undoCommand(new RevertDeleteLocaleCommand(locale));

		localeService.delete(locale);

		return redirect(routes.Application.projectLocales(locale.project.id));
	}

	public Result localeTranslate(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale);

		String referer = request().getHeader("Referer");

		if(referer == null)
			return redirect(routes.Application.locale(localeId));

		return redirect(referer);
	}

	public Result key(UUID id)
	{
		Key key = Key.byId(id);

		if(key == null)
			return redirect(routes.Application.index());

		select(key.project);

		Collections.sort(key.project.keys, (a, b) -> a.name.compareTo(b.name));

		List<Locale> locales = Locale.byProject(key.project);

		return ok(views.html.key.render(key, locales));
	}

	public Result keyCreate(UUID projectId, UUID localeId)
	{
		Key key = formFactory.form(Key.class).bindFromRequest().get();

		key.project = Project.byId(projectId);

		LOGGER.debug("Key: {}", Json.toJson(key));

		keyService.save(key);

		if(localeId != null)
		{
			Locale locale = Locale.byId(localeId);

			return redirect(routes.Application.locale(locale.id).withFragment("#key=" + key.name));
		}

		return redirect(routes.Application.key(key.id));
	}

	public Result keyEdit(UUID keyId)
	{
		Key key = Key.byId(keyId);

		if(key == null)
			return redirect(routes.Application.index());

		if("POST".equals(request().method()))
		{
			Key changed = formFactory.form(Key.class).bindFromRequest().get();

			key.name = changed.name;

			keyService.save(key);

			return redirect(routes.Application.projectKeys(key.project.id));
		}

		return ok(views.html.keyEdit.render(key));
	}

	public Result keyRemove(UUID keyId, UUID localeId)
	{
		Key key = Key.byId(keyId);

		LOGGER.debug("Key: {}", Json.toJson(key));

		if(key == null)
			return redirect(routes.Application.index());

		undoCommand(new RevertDeleteKeyCommand(key));

		keyService.delete(key);

		if(localeId != null)
		{
			Locale locale = Locale.byId(localeId);
			if(locale != null)
				return redirect(routes.Application.locale(locale.id));
		}

		LOGGER.debug("Go to projectKeys: {}", Json.toJson(key));

		return redirect(routes.Application.projectKeys(key.project.id));
	}

	public Result load()
	{
		String brand = ctx().messages().at("brand");
		Project project = Project.byName(brand);
		if(project == null)
		{
			project = projectService.save(new Project(brand));
		}
		else if(project.deleted)
		{
			project.deleted = false;
			projectService.save(project);
		}

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

		return redirect(controllers.routes.Application.project(project.id));
	}

	public Result commandExecute(String commandKey)
	{
		Command command = cache.get(commandKey);

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
				routes.javascript.Application.locale(),
				routes.javascript.Application.keyRemove(),
				routes.javascript.Api.getMessage(),
				routes.javascript.Api.putMessage(),
				routes.javascript.Api.findMessages()));
	}

	/**
	 * @param locale
	 * @param request
	 * @return
	 */
	private String importLocale(Locale locale, Request request)
	{
		MultipartFormData<File> body = request.body().asMultipartFormData();
		FilePart<File> messages = body.getFile("messages");

		if(messages == null)
			return null;

		ImportLocaleForm form = formFactory.form(ImportLocaleForm.class).bindFromRequest().get();

		LOGGER.debug("Type: {}", form.type);

		Importer importer;
		switch(FileType.fromKey(form.type))
		{
			case PlayMessages:
				importer = injector.instanceOf(PlayMessagesImporter.class);
			break;
			case JavaProperties:
				importer = injector.instanceOf(JavaPropertiesImporter.class);
			break;
			default:
				throw new IllegalArgumentException("File type " + form.type + " not supported yet");
		}

		try
		{
			importer.apply(messages.getFile(), locale);
		}
		catch(Exception e)
		{
			LOGGER.error("Error while importing messages", e);
		}

		return "OK";
	}

	/**
	 * @param command
	 */
	private String undoCommand(Command command)
	{
		String undoKey = String.format(COMMAND_FORMAT, UUID.randomUUID());

		cache.set(undoKey, command, 120);

		flash("undo", undoKey);

		return undoKey;
	}
}
