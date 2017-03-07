package controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
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
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;
import services.ProjectService;
import services.UserService;
import utils.FormUtils;

/**
 *
 * @author resamsel
 * @version 3 Oct 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Keys extends AbstractController {
  private static final Logger LOGGER = LoggerFactory.getLogger(Keys.class);

  private final FormFactory formFactory;

  private final Configuration configuration;

  private final KeyService keyService;

  private final LocaleService localeService;

  private final MessageService messageService;

  private final ProjectService projectService;

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   */
  @Inject
  protected Keys(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, FormFactory formFactory, Configuration configuration,
      KeyService keyService, LocaleService localeService, MessageService messageService,
      ProjectService projectService) {
    super(injector, cache, auth, userService, logEntryService);

    this.formFactory = formFactory;
    this.configuration = configuration;
    this.keyService = keyService;
    this.localeService = localeService;
    this.messageService = messageService;
    this.projectService = projectService;
  }

  public Result key(UUID id) {
    return key(id, key -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);

      Collections.sort(key.project.keys, (a, b) -> a.name.compareTo(b.name));

      List<Locale> locales = Locale.byProject(key.project);
      Map<UUID, Message> messages =
          messageService.findBy(new MessageCriteria().withKeyName(key.name)).getList().stream()
              .collect(Collectors.toMap(m -> m.locale.id, m -> m));

      return ok(views.html.keys.key.render(createTemplate(), key, locales, messages, form));
    });
  }

  public Result create(UUID projectId) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    return ok(views.html.keys.create.render(createTemplate(), project,
        formFactory.form(KeyForm.class).bindFromRequest()));
  }

  public Result doCreate(UUID projectId, UUID localeId) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    Form<KeyForm> form = formFactory.form(KeyForm.class).bindFromRequest();

    if (form.hasErrors())
      return badRequest(views.html.keys.create.render(createTemplate(), project, form));

    Key key = form.get().into(new Key());

    key.project = project;

    LOGGER.debug("Key: {}", Json.toJson(key));

    keyService.save(key);

    if (localeId != null) {
      Locale locale = localeService.byId(localeId);

      return redirect(routes.Locales.locale(locale.id).withFragment("#key=" + key.name));
    }

    return redirect(routes.Keys.key(key.id));
  }

  public Result createImmediately(UUID projectId, String keyName) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    if (keyName.length() > Key.NAME_LENGTH)
      return badRequest(views.html.keys.create.render(createTemplate(), project,
          formFactory.form(KeyForm.class).bind(ImmutableMap.of("name", keyName))));

    Key key = Key.byProjectAndName(project, keyName);

    if (key == null) {
      key = new Key(project, keyName);

      LOGGER.debug("Key: {}", Json.toJson(key));

      keyService.save(key);
    }

    return redirect(routes.Keys.key(key.id));
  }

  public Result edit(UUID keyId) {
    return key(keyId, key -> {
      return ok(views.html.keys.edit.render(createTemplate(), key,
          formFactory.form(KeyForm.class).fill(KeyForm.from(key))));
    });
  }

  public Result doEdit(UUID keyId) {
    return key(keyId, key -> {
      Form<KeyForm> form = formFactory.form(KeyForm.class).bindFromRequest();

      if (form.hasErrors())
        return badRequest(views.html.keys.edit.render(createTemplate(), key, form));

      keyService.save(form.get().into(key));

      return redirect(routes.Projects.keys(key.project.id, DEFAULT_SEARCH, DEFAULT_ORDER,
          DEFAULT_LIMIT, DEFAULT_OFFSET));
    });
  }

  public Result remove(UUID keyId, UUID localeId, String search, String order, int limit,
      int offset) {
    return key(keyId, key -> {
      undoCommand(injector.instanceOf(RevertDeleteKeyCommand.class).with(key));

      keyService.delete(key);

      if (localeId != null) {
        Locale locale = localeService.byId(localeId);
        if (locale != null)
          return redirect(routes.Locales.locale(locale.id));
      }

      LOGGER.debug("Go to projectKeys: {}", Json.toJson(key));

      return redirect(routes.Projects.keys(key.project.id, search, order, limit, offset));
    });
  }

  protected Result key(UUID keyId, Function<Key, Result> processor) {
    Key key = keyService.byId(keyId);
    if (key == null)
      return redirect(routes.Dashboards.dashboard());

    select(key.project);

    return processor.apply(key);
  }
}
