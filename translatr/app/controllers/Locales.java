package controllers;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteLocaleCommand;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import exporters.Exporter;
import exporters.JavaPropertiesExporter;
import exporters.PlayMessagesExporter;
import forms.ImportLocaleForm;
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
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.Request;
import play.mvc.Http.MultipartFormData.FilePart;
import services.LocaleService;
import services.UserService;
import utils.TransactionUtils;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 3 Oct 2016
 */
@SubjectPresent(forceBeforeAuthCheck = true)
public class Locales extends AbstractController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Locales.class);

	private final FormFactory formFactory;

	private final Configuration configuration;

	private final LocaleService localeService;

	/**
	 * @param injector
	 * @param cache
	 * @param auth
	 * @param userService
	 */
	@Inject
	protected Locales(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
				FormFactory formFactory, Configuration configuration, LocaleService localeService)
	{
		super(injector, cache, auth, userService);
		this.formFactory = formFactory;
		this.configuration = configuration;
		this.localeService = localeService;
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

		return ok(
			views.html.locales.locale.render(createTemplate(), locale.project, locale, keys, locales, messages, form));
	}

	public Result create(UUID projectId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		Form<LocaleForm> form = formFactory.form(LocaleForm.class).bindFromRequest();

		if(form.hasErrors())
			return badRequest(views.html.locales.create.render(createTemplate(), project, form));

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

		return redirect(routes.Locales.locale(locale.id));
	}

	public Result createImmediately(UUID projectId, String localeName)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		if(localeName.length() > Locale.NAME_LENGTH)
			return badRequest(
				views.html.locales.create.render(
					createTemplate(),
					project,
					formFactory.form(LocaleForm.class).bind(ImmutableMap.of("name", localeName))));

		Locale locale = Locale.byProjectAndName(project, localeName);

		if(locale == null)
		{
			locale = new Locale(project, localeName);

			LOGGER.debug("Locale: {}", Json.toJson(locale));

			localeService.save(locale);
		}

		return redirect(routes.Locales.locale(locale.id));
	}

	public Result edit(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		if("POST".equals(request().method()))
		{
			Form<LocaleForm> form = formFactory.form(LocaleForm.class).bindFromRequest();

			if(form.hasErrors())
				return badRequest(views.html.locales.edit.render(createTemplate(), locale, form));

			localeService.save(form.get().into(locale));

			return redirect(routes.Projects.locales(locale.project.id));
		}

		return ok(
			views.html.locales.edit
				.render(createTemplate(), locale, formFactory.form(LocaleForm.class).fill(LocaleForm.from(locale))));
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
			default:
				return badRequest("File type " + fileType + " not supported yet");
		}

		exporter.addHeaders(response(), locale);

		return ok(new ByteArrayInputStream(exporter.apply(locale)));
	}

	public Result upload(UUID id)
	{
		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		if("POST".equals(request().method()))
		{
			importLocale(locale, request());

			return redirect(routes.Locales.locale(id));
		}
		else
		{
			return ok(views.html.locales.upload.render(createTemplate(), locale.project, locale));
		}
	}

	public Result keysSearch(UUID localeId)
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

	public Result remove(UUID localeId)
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

	public Result translate(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale);

		String referer = request().getHeader("Referer");

		if(referer == null)
			return redirect(routes.Locales.locale(localeId));

		return redirect(referer);
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
