package controllers;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

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

import com.google.common.collect.ImmutableMap;

import actions.ContextAction;
import commands.Command;
import commands.RevertDeleteKeyCommand;
import commands.RevertDeleteLocaleCommand;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import exporters.Exporter;
import exporters.JavaPropertiesExporter;
import exporters.PlayMessagesExporter;
import forms.ImportLocaleForm;
import forms.KeyForm;
import forms.LocaleForm;
import forms.SearchForm;
import importers.Importer;
import importers.JavaPropertiesImporter;
import importers.PlayMessagesImporter;
import models.FileType;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Call;
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
public class Application extends AbstractController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private final Injector injector;

	private final FormFactory formFactory;

	private final ProjectService projectService;

	private final LocaleService localeService;

	private final KeyService keyService;

	private final MessageService messageService;

	private final Configuration configuration;

	@Inject
	public Application(CacheApi cache, Injector injector, FormFactory formFactory, ProjectService projectService,
				LocaleService localeService, KeyService keyService, MessageService messageService,
				Configuration configuration)
	{
		super(cache);

		this.injector = injector;
		this.formFactory = formFactory;
		this.projectService = projectService;
		this.localeService = localeService;
		this.keyService = keyService;
		this.messageService = messageService;
		this.configuration = configuration;
	}

	public Result index()
	{
		return ok(views.html.index.render());
	}

	public Result locale(UUID id)
	{
		LOGGER.debug("locale(id={})", id);

		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(locale.project.id).withLocaleId(locale.id));
		search.pager(keys);
		List<Locale> locales = Locale.findBy(new LocaleCriteria().withProjectId(locale.project.id).withLimit(100));
		Map<String, Message> messages = Message.findBy(new MessageCriteria().withLocaleId(locale.id)).stream().collect(
			Collectors.groupingBy((m) -> m.key.name, Collectors.reducing(null, a -> a, (a, b) -> b)));

		return ok(views.html.locales.locale.render(locale.project, locale, keys, locales, messages, form));
	}

	public Result localeKeysSearch(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		SearchForm search = SearchForm.bindFromRequest(formFactory, configuration).get();

		List<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(locale.project.id).withLocaleId(locale.id));
		search.pager(keys);

		Map<String, Message> messages = Message
			.findBy(
				new MessageCriteria()
					.withLocaleId(locale.id)
					.withKeyIds(keys.stream().map(k -> k.id).collect(Collectors.toList())))
			.stream()
			.collect(groupingBy(m -> m.key.name, reducing(null, a -> a, (a, b) -> b)));

		LOGGER.debug("Keys found {} for {}", keys.size(), search);

		return ok(views.html.tags.keyItems.render(keys, messages, search.hasMore));
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

		Form<LocaleForm> form = formFactory.form(LocaleForm.class).bindFromRequest();

		if(form.hasErrors())
			return badRequest(views.html.locales.create.render(project, form));

		LOGGER.debug("Locale: {}", Json.toJson(form));

		Locale locale = form.get().into(new Locale());

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

	public Result localeCreateImmediately(UUID projectId, String localeName)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		if(localeName.length() > Locale.NAME_LENGTH)
			return badRequest(
				views.html.locales.create
					.render(project, formFactory.form(LocaleForm.class).bind(ImmutableMap.of("name", localeName))));

		Locale locale = Locale.byProjectAndName(project, localeName);

		if(locale == null)
		{
			locale = new Locale(project, localeName);

			LOGGER.debug("Locale: {}", Json.toJson(locale));

			localeService.save(locale);
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
			Form<LocaleForm> form = formFactory.form(LocaleForm.class).bindFromRequest();

			if(form.hasErrors())
				return badRequest(views.html.locales.edit.render(locale, form));

			localeService.save(form.get().into(locale));

			return redirect(routes.Projects.locales(locale.project.id));
		}

		return ok(
			views.html.locales.edit.render(locale, formFactory.form(LocaleForm.class).fill(LocaleForm.from(locale))));
	}

	public Result localeRemove(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale);

		LOGGER.debug("Creating undo command");

		undoCommand(injector.instanceOf(RevertDeleteLocaleCommand.class).with(locale));

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

		return redirect(routes.Projects.locales(locale.project.id));
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

		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);

		Collections.sort(key.project.keys, (a, b) -> a.name.compareTo(b.name));

		List<Locale> locales = Locale.byProject(key.project);
		Map<UUID, Message> messages = Message.findBy(new MessageCriteria().withKeyName(key.name)).stream().collect(
			groupingBy(m -> m.locale.id, Collectors.reducing(null, a -> a, (a, b) -> b)));

		return ok(views.html.keys.key.render(key, locales, messages, form));
	}

	public Result keyCreate(UUID projectId, UUID localeId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<KeyForm> form = formFactory.form(KeyForm.class).bindFromRequest();

		if(form.hasErrors())
			return badRequest(views.html.keys.create.render(project, form));

		Key key = form.get().into(new Key());

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

		if(keyName.length() > Key.NAME_LENGTH)
			return badRequest(
				views.html.keys.create
					.render(project, formFactory.form(KeyForm.class).bind(ImmutableMap.of("name", keyName))));

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
				return badRequest(views.html.keys.edit.render(key, form));

			keyService.save(form.get().into(key));

			return redirect(routes.Projects.keys(key.project.id));
		}

		return ok(views.html.keys.edit.render(key, formFactory.form(KeyForm.class).fill(KeyForm.from(key))));
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
				return redirect(routes.Application.locale(locale.id));
		}

		LOGGER.debug("Go to projectKeys: {}", Json.toJson(key));

		return redirect(routes.Projects.keys(key.project.id));
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
				routes.javascript.Application.localeKeysSearch(),
				routes.javascript.Application.locale(),
				routes.javascript.Application.key(),
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
}
