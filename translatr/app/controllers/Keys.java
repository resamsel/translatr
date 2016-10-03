package controllers;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteKeyCommand;
import criterias.MessageCriteria;
import forms.KeyForm;
import forms.SearchForm;
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
import play.mvc.With;
import services.KeyService;
import services.UserService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 3 Oct 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Keys extends AbstractController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Keys.class);

	private final FormFactory formFactory;

	private final Configuration configuration;

	private final KeyService keyService;

	/**
	 * @param injector
	 * @param cache
	 * @param auth
	 * @param userService
	 */
	@Inject
	protected Keys(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
				FormFactory formFactory, Configuration configuration, KeyService keyService)
	{
		super(injector, cache, auth, userService);
		this.formFactory = formFactory;
		this.configuration = configuration;
		this.keyService = keyService;
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

	public Result create(UUID projectId, UUID localeId)
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

		return redirect(routes.Keys.key(key.id));
	}

	public Result createImmediately(UUID projectId, String keyName)
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

		return redirect(routes.Keys.key(key.id));
	}

	public Result edit(UUID keyId)
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

	public Result remove(UUID keyId, UUID localeId)
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
}
