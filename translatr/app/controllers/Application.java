package controllers;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static utils.FormatUtils.formatLocale;
import static utils.Stopwatch.log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.ContextAction;
import commands.Command;
import commands.RevertDeleteKeyCommand;
import commands.RevertDeleteLocaleCommand;
import commands.RevertDeleteProjectCommand;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import exporters.Exporter;
import exporters.JavaPropertiesExporter;
import exporters.PlayMessagesExporter;
import forms.ImportLocaleForm;
import forms.KeyForm;
import forms.LocaleForm;
import forms.ProjectForm;
import forms.SearchForm;
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
import utils.TransactionUtils;

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
		ctx().args.put("projectId", project.id);
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
		SearchForm form = formFactory.form(SearchForm.class).bindFromRequest().get();

		return ok(views.html.dashboard.render(Project.all(), form));
	}

	public Result project(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		return ok(log(() -> views.html.project.render(project), LOGGER, "Rendering project"));
	}

	public Result projectCreate()
	{
		ProjectForm form = formFactory.form(ProjectForm.class).bindFromRequest().get();

		LOGGER.debug("Project: {}", Json.toJson(form));

		Project project = Project.byName(form.getName());
		if(project != null)
			form.fill(project).withDeleted(false);
		else
			project = form.fill(new Project());
		projectService.save(project);

		select(project);

		return redirect(routes.Application.project(project.id));
	}

	public Result projectEdit(UUID projectId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		if("POST".equals(request().method()))
		{
			ProjectForm form = formFactory.form(ProjectForm.class).bindFromRequest().get();

			projectService.save(form.fill(project));

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

		select(project);

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

		SearchForm form = formFactory.form(SearchForm.class).bindFromRequest().get();

		List<Locale> locales = Locale.findBy(
			new LocaleCriteria()
				.withProjectId(project.id)
				.withSearch(form.search)
				.withOffset(form.offset)
				.withLimit(form.limit));
		java.util.Locale locale = ctx().lang().locale();
		Collections.sort(locales, (a, b) -> formatLocale(locale, a).compareTo(formatLocale(locale, b)));

		return ok(
			log(
				() -> views.html.projectLocales.render(
					project,
					locales,
					localeService
						.progress(locales.stream().map(l -> l.id).collect(Collectors.toList()), Key.countBy(project)),
					form),
				LOGGER,
				"Rendering projectLocales"));
	}

	public Result projectKeys(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		SearchForm form = formFactory.form(SearchForm.class).bindFromRequest().get();

		List<Key> keys = Key.findBy(
			new KeyCriteria()
				.withProjectId(project.id)
				.withSearch(form.search)
				.withOffset(form.offset)
				.withLimit(form.limit)
				.withOrder("name"));

		Map<UUID, Double> progress =
					keyService.progress(keys.stream().map(k -> k.id).collect(Collectors.toList()), Locale.countBy(project));

		return ok(
			views.html.projectKeys.render(
				project,
				keys.size() > form.limit ? keys.subList(0, form.limit) : keys,
				progress,
				keys.size() > form.limit,
				form));
	}

	public Result projectKeysSearch(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		SearchForm form = formFactory.form(SearchForm.class).bindFromRequest().get();

		List<Key> keys = Key.findBy(
			new KeyCriteria()
				.withProjectId(project.id)
				.withSearch(form.search)
				.withMissing(form.missing)
				.withOffset(form.offset)
				.withLimit(form.limit)
				.withOrder("name"));

		Map<UUID, Double> progress =
					keyService.progress(keys.stream().map(k -> k.id).collect(Collectors.toList()), Locale.countBy(project));

		return ok(
			views.html.tags.keyRows
				.render(keys.size() > form.limit ? keys.subList(0, form.limit) : keys, progress, keys.size() > form.limit));
	}

	public Result locale(UUID id)
	{
		LOGGER.debug("locale(id={})", id);

		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		SearchForm form = formFactory.form(SearchForm.class).bindFromRequest().get();

		List<Key> keys = Key.findBy(
			new KeyCriteria()
				.withProjectId(locale.project.id)
				.withSearch(form.search)
				.withMissing(form.missing)
				.withLocaleId(locale.id)
				.withOffset(form.offset)
				.withLimit(form.limit)
				.withOrder("name"));
		List<Locale> locales = Locale.findBy(new LocaleCriteria().withProjectId(locale.project.id).withLimit(100));
		Map<String, Message> messages = Message.findBy(new MessageCriteria().withLocaleId(locale.id)).stream().collect(
			Collectors.groupingBy((m) -> m.key.name, Collectors.reducing(null, (a) -> a, (a, b) -> b)));

		return ok(
			views.html.locale.render(
				locale.project,
				locale,
				keys.size() > form.limit ? keys.subList(0, form.limit) : keys,
				locales,
				messages,
				form));
	}

	public Result localeKeysSearch(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		SearchForm form = formFactory.form(SearchForm.class).bindFromRequest().get();

		List<Key> keys = Key.findBy(
			new KeyCriteria()
				.withProjectId(locale.project.id)
				.withSearch(form.search)
				.withMissing(form.missing)
				.withLocaleId(locale.id)
				.withOffset(form.offset)
				.withLimit(form.limit)
				.withOrder("name"));

		Map<String, Message> messages = Message
			.findBy(
				new MessageCriteria()
					.withLocaleId(locale.id)
					.withKeyIds(keys.stream().map(k -> k.id).collect(Collectors.toList())))
			.stream()
			.collect(groupingBy(m -> m.key.name, reducing(null, (a) -> a, (a, b) -> b)));

		LOGGER.debug("Keys found {} for {}", keys.size(), form);

		return ok(
			views.html.tags.keyItems
				.render(keys.size() > form.limit ? keys.subList(0, form.limit) : keys, messages, keys.size() > form.limit));
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

		select(project);

		LocaleForm form = formFactory.form(LocaleForm.class).bindFromRequest().get();

		LOGGER.debug("Locale: {}", Json.toJson(form));

		Locale locale = form.fill(new Locale());

		locale.project = project;

		localeService.save(locale);

		try
		{
			importLocale(locale, request());
		}
		catch(IllegalStateException e)
		{
			// This is OK, the fileType doesn't need to be filled
		}

		return redirect(routes.Application.locale(locale.id));
	}

	public Result localeEdit(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		if("POST".equals(request().method()))
		{
			LocaleForm form = formFactory.form(LocaleForm.class).bindFromRequest().get();

			localeService.save(form.fill(locale));

			return redirect(routes.Application.projectLocales(locale.project.id));
		}

		return ok(views.html.localeEdit.render(locale));
	}

	public Result localeRemove(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale);

		LOGGER.debug("Creating undo command");

		undoCommand(new RevertDeleteLocaleCommand(locale));

		LOGGER.debug("Excuting batch delete");

		try
		{
			TransactionUtils.batchExecute((tx) -> {
				localeService.delete(locale);
			});
		}
		catch(Exception e)
		{
			LOGGER.error("Error while batch deleting locale", e);
		}

		LOGGER.debug("Redirecting");

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

		SearchForm form = formFactory.form(SearchForm.class).bindFromRequest().get();

		Collections.sort(key.project.keys, (a, b) -> a.name.compareTo(b.name));

		List<Locale> locales = Locale.byProject(key.project);
		Map<UUID, Message> messages = Message.findBy(new MessageCriteria().withKeyName(key.name)).stream().collect(
			groupingBy(m -> m.locale.id, Collectors.reducing(null, (a) -> a, (a, b) -> b)));

		return ok(views.html.key.render(key, locales, messages, form));
	}

	public Result keyCreate(UUID projectId, UUID localeId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		KeyForm form = formFactory.form(KeyForm.class).bindFromRequest().get();

		Key key = form.fill(new Key());

		key.project = project;

		LOGGER.debug("Key: {}", Json.toJson(key));

		keyService.save(key);

		if(localeId != null)
		{
			Locale locale = Locale.byId(localeId);

			return redirect(routes.Application.locale(locale.id).withFragment("#key=" + key.name));
		}

		return redirect(routes.Application.key(key.id));
	}

	public Result keyCreateImmediately(UUID projectId, String keyName)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

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
			KeyForm form = formFactory.form(KeyForm.class).bindFromRequest().get();

			keyService.save(form.fill(key));

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

		select(key.project);

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
				routes.javascript.Application.projectKeysSearch(),
				routes.javascript.Application.localeKeysSearch(),
				routes.javascript.Application.locale(),
				routes.javascript.Application.keyCreateImmediately(),
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

		LOGGER.debug("Type: {}", form.getFileType());

		Importer importer;
		switch(FileType.fromKey(form.getFileType()))
		{
			case PlayMessages:
				importer = injector.instanceOf(PlayMessagesImporter.class);
			break;
			case JavaProperties:
				importer = injector.instanceOf(JavaPropertiesImporter.class);
			break;
			default:
				throw new IllegalArgumentException("File type " + form.getFileType() + " not supported yet");
		}

		try
		{
			importer.apply(messages.getFile(), locale);
		}
		catch(Exception e)
		{
			LOGGER.error("Error while importing messages", e);
		}

		LOGGER.debug("End of import");

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
