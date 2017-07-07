package controllers;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;
import commands.RevertDeleteKeyCommand;
import criterias.KeyCriteria;
import forms.KeyForm;
import forms.LocaleSearchForm;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import models.Key;
import models.Locale;
import models.Project;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.With;
import services.KeyService;
import services.LocaleService;
import utils.FormUtils;
import utils.Template;

/**
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

  @Inject
  protected Keys(Injector injector, CacheApi cache, PlayAuthenticate auth, FormFactory formFactory,
      Configuration configuration, KeyService keyService, LocaleService localeService) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.configuration = configuration;
    this.keyService = keyService;
    this.localeService = localeService;
  }

  public CompletionStage<Result> keyBy(String username, String projectName, String keyName,
      String search, String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> key(project, keyName, key -> {
      Form<LocaleSearchForm> form =
          FormUtils.LocaleSearch.bindFromRequest(formFactory, configuration);
      form.get().update(search, order, limit, offset);

      return ok(views.html.keys.key.render(createTemplate(), key, form));
    }), User.FETCH_PROJECTS, User.FETCH_PROJECTS + ".keys");
  }

  public CompletionStage<Result> createBy(String username, String projectName) {
    return project(username, projectName,
        (user, project) -> ok(views.html.keys.create.render(createTemplate(), project,
            FormUtils.Key.bindFromRequest(formFactory, configuration))));
  }

  public CompletionStage<Result> doCreateBy(String username, String projectName, UUID localeId,
      String search, String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<KeyForm> form = FormUtils.Key.bindFromRequest(formFactory, configuration);

      if (form.hasErrors()) {
        return badRequest(views.html.keys.create.render(createTemplate(), project, form));
      }

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
    });
  }

  public CompletionStage<Result> createImmediatelyBy(String username, String projectName,
      String keyName, String search, String order,
      int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<KeyForm> form =
          KeyForm.with(keyName, FormUtils.Key.bindFromRequest(formFactory, configuration));
      if (keyName.length() > Key.NAME_LENGTH) {
        return badRequest(views.html.keys.create.render(createTemplate(), project, form));
      }

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
    });
  }

  public CompletionStage<Result> editBy(String username, String projectName, String keyName,
      String search, String order, int limit, int offset) {
    return key(username, projectName, keyName,
        (project, key) -> ok(views.html.keys.edit.render(createTemplate(), key,
            KeyForm.with(key, FormUtils.Key.bindFromRequest(formFactory, configuration))))
    );
  }

  public CompletionStage<Result> doEditBy(String username, String projectName, String keyName) {
    return key(username, projectName, keyName, (project, key) -> {
      Form<KeyForm> form = FormUtils.Key.bindFromRequest(formFactory, configuration);

      if (form.hasErrors()) {
        return badRequest(views.html.keys.edit.render(createTemplate(), key, form));
      }

      try {
        keyService.save(form.get().into(key));
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.keys.edit.render(createTemplate(), key, FormUtils.include(form, e)));
      }

      KeyForm search = form.get();

      return redirect(routes.Projects.keysBy(key.project.owner.username, key.project.name,
          search.search, search.order, search.limit, search.offset));
    });
  }

  public CompletionStage<Result> removeBy(String username, String projectName, String keyName,
      UUID localeId, String search, String order, int limit, int offset) {
    return key(username, projectName, keyName, (project, key) -> {
      undoCommand(RevertDeleteKeyCommand.from(key));

      keyService.delete(key);

      if (localeId != null) {
        Locale locale = localeService.byId(localeId);
        if (locale != null) {
          return redirect(locale.route(search, order, limit, offset));
        }
      }

      LOGGER.debug("Go to projectKeys: {}", Json.toJson(key));

      return redirect(routes.Projects.keysBy(key.project.owner.username, key.project.name, search,
          order, limit, offset));
    });
  }

  private Result key(Project project, String keyName, Function<Key, Result> processor) {
    if (project.keys != null) {
      Optional<Key> key = project.keys.stream().filter(k -> k.name.equals(keyName)).findFirst();
      if (key.isPresent()) {
        return processor.apply(key.get());
      }
    }

    PagedList<Key> keys = keyService
        .findBy(new KeyCriteria().withProjectId(project.id).withNames(Arrays.asList(keyName)));
    if (keys.getList().isEmpty()) {
      return redirect(project.route());
    }

    select(project);

    return processor.apply(keys.getList().get(0));
  }

  private CompletionStage<Result> key(String username, String projectName, String keyName,
      BiFunction<Project, Key, Result> processor) {
    return project(username, projectName,
        (user, project) -> key(project, keyName, key -> processor.apply(project, key)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_PROJECTS);
  }

  /**
   * @param project
   * @return
   */
  public static Call doCreateRoute(Project project) {
    return doCreateRoute(project, null);
  }

  /**
   * @param project
   * @return
   */
  public static Call doCreateRoute(Project project, UUID localeId) {
    return routes.Keys
        .doCreateBy(project.owner.username, project.name, localeId, DEFAULT_SEARCH, DEFAULT_ORDER,
            DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public static Call createImmediatelyRoute(dto.Project project, String keyName, String search,
      String order, int limit, int offset) {
    return routes.Keys
        .createImmediatelyBy(project.ownerUsername, project.name, keyName, search, order, limit,
            offset);
  }
}
