package controllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteKeyCommand;
import criterias.KeyCriteria;
import forms.KeyForm;
import forms.LocaleSearchForm;
import models.Key;
import models.Locale;
import models.Project;
import models.User;
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
import utils.Template;

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

  public CompletionStage<Result> keyBy(String username, String projectPath, String keyName,
      String search, String order, int limit, int offset) {
    return user(username,
        user -> project(user, projectPath, project -> key(project, keyName, key -> {
          Form<LocaleSearchForm> form =
              FormUtils.LocaleSearch.bindFromRequest(formFactory, configuration);
          form.get().update(search, order, limit, offset);

          return ok(views.html.keys.key.render(createTemplate(), key, form));
        })), User.FETCH_PROJECTS, User.FETCH_PROJECTS + ".keys");
  }

  public Result key(UUID id, String search, String order, int limit, int offset) {
    return key(id, key -> {
      Form<LocaleSearchForm> form =
          FormUtils.LocaleSearch.bindFromRequest(formFactory, configuration);

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

    try {
      keyService.save(key);
    } catch (ConstraintViolationException e) {
      return badRequest(
          views.html.keys.create.render(createTemplate(), project, FormUtils.include(form, e)));
    }

    if (localeId != null) {
      Locale locale = localeService.byId(localeId);

      return redirect(locale.route(search, order, limit, offset).withFragment("key/" + key.name));
    }

    return redirect(key.route(search, order, limit, offset));
  }

  public Result createImmediately(UUID projectId, String keyName, String search, String order,
      int limit, int offset) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    Form<KeyForm> form =
        KeyForm.with(keyName, FormUtils.Key.bindFromRequest(formFactory, configuration));
    if (keyName.length() > Key.NAME_LENGTH)
      return badRequest(views.html.keys.create.render(createTemplate(), project, form));

    Key key = Key.byProjectAndName(project, keyName);

    if (key == null) {
      key = new Key(project, keyName);

      LOGGER.debug("Key: {}", Json.toJson(key));

      try {
        keyService.save(key);
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.keys.create.render(createTemplate(), project, FormUtils.include(form, e)));
      }
    }

    return redirect(key.route());
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

      try {
        keyService.save(form.get().into(key));
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.keys.edit.render(createTemplate(), key, FormUtils.include(form, e)));
      }

      KeyForm search = form.get();

      return redirect(routes.Projects.keysBy(key.project.owner.username, key.project.path,
          search.search, search.order, search.limit, search.offset));
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
          return redirect(locale.route(search, order, limit, offset));
      }

      LOGGER.debug("Go to projectKeys: {}", Json.toJson(key));

      return redirect(routes.Projects.keysBy(key.project.owner.username, key.project.path, search,
          order, limit, offset));
    });
  }

  private Result project(User user, String projectPath, Function<Project, Result> processor) {
    if (user.projects != null) {
      Optional<Project> project =
          user.projects.stream().filter(p -> p.path.equals(projectPath)).findFirst();
      if (project.isPresent())
        return processor.apply(project.get());
    }

    Project project = projectService.byOwnerAndPath(user, projectPath);
    if (project == null)
      return redirectWithError(routes.Application.index(), "project.notFound");

    return processor.apply(project);
  }

  private Result key(Project project, String keyName, Function<Key, Result> processor) {
    if (project.keys != null) {
      Optional<Key> key = project.keys.stream().filter(k -> k.name.equals(keyName)).findFirst();
      if (key.isPresent())
        return processor.apply(key.get());
    }

    PagedList<Key> keys = keyService
        .findBy(new KeyCriteria().withProjectId(project.id).withNames(Arrays.asList(keyName)));
    if (keys.getList().isEmpty())
      return redirect(project.route());

    select(project);

    return processor.apply(keys.getList().get(0));
  }


  protected Result key(UUID keyId, Function<Key, Result> processor) {
    Key key = keyService.byId(keyId, "project");
    if (key == null)
      return redirect(routes.Projects.index());

    select(key.project);

    return processor.apply(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_PROJECTS);
  }
}
