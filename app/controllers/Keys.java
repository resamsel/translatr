package controllers;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteKeyCommand;
import forms.KeyForm;
import forms.SearchForm;
import models.Key;
import models.Locale;
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
import services.ProjectService;
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

  private final ProjectService projectService;

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   */
  @Inject
  protected Keys(Injector injector, CacheApi cache, PlayAuthenticate auth, FormFactory formFactory,
      Configuration configuration, KeyService keyService, LocaleService localeService,
      ProjectService projectService) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.configuration = configuration;
    this.keyService = keyService;
    this.localeService = localeService;
    this.projectService = projectService;
  }

  public Result key(UUID id, String search, String order, int limit, int offset) {
    return key(id, key -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);

      Collections.sort(key.project.keys, (a, b) -> a.name.compareTo(b.name));

      return ok(views.html.keys.key.render(createTemplate(), key, form));
    });
  }

  public Result create(UUID projectId) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    return ok(views.html.keys.create.render(createTemplate(), project,
        FormUtils.Key.bindFromRequest(formFactory, configuration)));
  }

  public Result doCreate(UUID projectId, UUID localeId, String search, String order, int limit,
      int offset) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    Form<KeyForm> form = FormUtils.Key.bindFromRequest(formFactory, configuration);

    if (form.hasErrors())
      return badRequest(views.html.keys.create.render(createTemplate(), project, form));

    Key key = form.get().into(new Key());

    key.project = project;

    LOGGER.debug("Key: {}", Json.toJson(key));

    keyService.save(key);

    if (localeId != null) {
      Locale locale = localeService.byId(localeId);

      return redirect(routes.Locales.locale(locale.id, search, order, limit, offset)
          .withFragment("key/" + key.name));
    }

    return redirect(routes.Keys.key(key.id, search, order, limit, offset));
  }

  public Result createImmediately(UUID projectId, String keyName, String search, String order,
      int limit, int offset) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    if (keyName.length() > Key.NAME_LENGTH)
      return badRequest(views.html.keys.create.render(createTemplate(), project,
          KeyForm.with(keyName, FormUtils.Key.bindFromRequest(formFactory, configuration))));

    Key key = Key.byProjectAndName(project, keyName);

    if (key == null) {
      key = new Key(project, keyName);

      LOGGER.debug("Key: {}", Json.toJson(key));

      keyService.save(key);
    }

    return redirect(routes.Keys.key(key.id, search, order, limit, offset));
  }

  public Result edit(UUID keyId, String search, String order, int limit, int offset) {
    return key(keyId, key -> {

      return ok(views.html.keys.edit.render(createTemplate(), key,
          KeyForm.with(key, FormUtils.Key.bindFromRequest(formFactory, configuration))));
    });
  }

  public Result doEdit(UUID keyId) {
    return key(keyId, key -> {
      Form<KeyForm> form = FormUtils.Key.bindFromRequest(formFactory, configuration);

      if (form.hasErrors())
        return badRequest(views.html.keys.edit.render(createTemplate(), key, form));

      keyService.save(form.get().into(key));

      KeyForm search = form.get();

      return redirect(routes.Projects.keys(key.project.id, search.search, search.order,
          search.limit, search.offset));
    });
  }

  public Result remove(UUID keyId, UUID localeId, String search, String order, int limit,
      int offset) {
    return key(keyId, key -> {
      undoCommand(RevertDeleteKeyCommand.from(key));

      keyService.delete(key);

      if (localeId != null) {
        Locale locale = localeService.byId(localeId);
        if (locale != null)
          return redirect(routes.Locales.locale(locale.id, search, order, limit, offset));
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
