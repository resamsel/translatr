package controllers;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteLocaleCommand;
import criterias.LocaleCriteria;
import forms.KeySearchForm;
import forms.LocaleForm;
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
import services.LocaleService;
import services.ProjectService;
import services.api.LocaleApiService;
import utils.FormUtils;
import utils.Template;
import utils.TransactionUtils;

/**
 *
 * @author resamsel
 * @version 3 Oct 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Locales extends AbstractController {
  private static final Logger LOGGER = LoggerFactory.getLogger(Locales.class);

  private final FormFactory formFactory;

  private final Configuration configuration;

  private final LocaleService localeService;

  private final LocaleApiService localeApiService;

  private final ProjectService projectService;

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   */
  @Inject
  protected Locales(Injector injector, CacheApi cache, PlayAuthenticate auth,
      FormFactory formFactory, Configuration configuration, LocaleService localeService,
      LocaleApiService localeApiService, ProjectService projectService) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.configuration = configuration;
    this.localeService = localeService;
    this.localeApiService = localeApiService;
    this.projectService = projectService;
  }

  public Result localeBy(String username, String projectName, String localeName) {
    return user(username, user -> project(user, projectName, project -> locale(project, localeName,
        locale -> locale(locale.id, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT, DEFAULT_OFFSET))),
        User.FETCH_PROJECTS, User.FETCH_PROJECTS + ".locales");
  }


  public Result locale(UUID localeId, String search, String order, int limit, int offset) {
    return locale(localeId, locale -> {
      Form<KeySearchForm> form = FormUtils.KeySearch.bindFromRequest(formFactory, configuration);
      KeySearchForm s = form.get();
      if (s.order == null)
        s.order = "name";

      return ok(views.html.locales.locale.render(createTemplate(), locale, form));
    });
  }

  public Result doCreate(UUID projectId) {
    Project project = projectService.byId(projectId);
    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    Form<LocaleForm> form = FormUtils.Locale.bindFromRequest(formFactory, configuration);

    if (form.hasErrors())
      return badRequest(views.html.locales.create.render(createTemplate(), project, form));

    LOGGER.debug("Locale: {}", Json.toJson(form));

    Locale locale = form.get().into(new Locale());

    locale.project = project;

    try {
      localeService.save(locale);
    } catch (ConstraintViolationException e) {
      return badRequest(
          views.html.locales.create.render(createTemplate(), project, FormUtils.include(form, e)));
    }

    try {
      localeApiService.upload(locale.id, request());
    } catch (IllegalStateException e) {
      // This is OK, the fileType doesn't need to be filled
    }

    LocaleForm search = form.get();

    return redirect(
        routes.Locales.locale(locale.id, search.search, search.order, search.limit, search.offset));
  }

  public Result createImmediately(UUID projectId, String localeName, String search, String order,
      int limit, int offset) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    Form<LocaleForm> form =
        LocaleForm.with(localeName, FormUtils.Locale.bindFromRequest(formFactory, configuration));
    if (localeName.length() > Locale.NAME_LENGTH)
      return badRequest(views.html.locales.create.render(createTemplate(), project, form));

    Locale locale = Locale.byProjectAndName(project, localeName);

    if (locale == null) {
      locale = new Locale(project, localeName);

      LOGGER.debug("Locale: {}", Json.toJson(locale));

      try {
        localeService.save(locale);
      } catch (ConstraintViolationException e) {
        return badRequest(views.html.locales.create.render(createTemplate(), project,
            FormUtils.include(form, e)));
      }
    }

    return redirect(routes.Locales.locale(locale.id, search, order, limit, offset));
  }

  public Result edit(UUID localeId, String search, String order, int limit, int offset) {
    return locale(localeId, locale -> {
      return ok(views.html.locales.edit.render(createTemplate(), locale,
          LocaleForm.with(locale, FormUtils.Locale.bindFromRequest(formFactory, configuration))));
    });
  }

  public Result doEdit(UUID localeId) {
    return locale(localeId, locale -> {
      Form<LocaleForm> form = FormUtils.Locale.bindFromRequest(formFactory, configuration);

      if (form.hasErrors())
        return badRequest(views.html.locales.edit.render(createTemplate(), locale, form));

      localeService.save(form.get().into(locale));

      LocaleForm search = form.get();
      return redirect(routes.Projects.locales(locale.project.id, search.search, search.order,
          search.limit, search.offset));
    });
  }

  public Result upload(UUID localeId) {
    return locale(localeId, locale -> {
      return ok(views.html.locales.upload.render(createTemplate(), locale.project, locale));
    });
  }

  public Result doUpload(UUID localeId) {
    return locale(localeId, locale -> {
      try {
        localeApiService.upload(localeId, request());
      } catch (Exception e) {
        addError(e.getMessage());
        return badRequest(
            views.html.locales.upload.render(createTemplate(), locale.project, locale));
      }

      return redirect(routes.Locales.locale(localeId, DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT,
          DEFAULT_OFFSET));
    });
  }

  public Result remove(UUID localeId, String s, String order, int limit, int offset) {
    return locale(localeId, locale -> {
      undoCommand(RevertDeleteLocaleCommand.from(locale));

      try {
        TransactionUtils.batchExecute((tx) -> {
          localeService.delete(locale);
        });
      } catch (Exception e) {
        LOGGER.error("Error while batch deleting locale", e);
      }

      return redirect(routes.Projects.locales(locale.project.id, s, order, limit, offset));
    });
  }

  private Result project(User user, String projectName, Function<Project, Result> processor) {
    if (user.projects != null) {
      Optional<Project> project =
          user.projects.stream().filter(p -> p.name.equals(projectName)).findFirst();
      if (project.isPresent())
        return processor.apply(project.get());
    }

    Project project = projectService.byOwnerAndName(user, projectName);
    if (project == null)
      return redirectWithError(routes.Application.index(), "project.notFound");

    return processor.apply(project);
  }

  private Result locale(Project project, String localeName, Function<Locale, Result> processor) {
    if (project.locales != null) {
      Optional<Locale> locale =
          project.locales.stream().filter(l -> l.name.equals(localeName)).findFirst();
      if (locale.isPresent())
        return processor.apply(locale.get());
    }

    PagedList<Locale> locales = localeService
        .findBy(new LocaleCriteria().withProjectId(project.id).withLocaleName(localeName));
    if (locales.getList().isEmpty())
      return redirect(routes.Projects.project(project.id));

    select(project);

    return processor.apply(locales.getList().get(0));
  }

  private Result locale(UUID localeId, Function<Locale, Result> processor) {
    Locale locale = localeService.byId(localeId);
    if (locale == null)
      return redirect(routes.Projects.index());

    select(locale.project);

    return processor.apply(locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_PROJECTS);
  }
}
