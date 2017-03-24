package controllers;

import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteLocaleCommand;
import forms.KeySearchForm;
import forms.LocaleForm;
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
import services.LocaleService;
import services.LogEntryService;
import services.ProjectService;
import services.UserService;
import services.api.LocaleApiService;
import utils.FormUtils;
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
      UserService userService, LogEntryService logEntryService, FormFactory formFactory,
      Configuration configuration, LocaleService localeService, LocaleApiService localeApiService,
      ProjectService projectService) {
    super(injector, cache, auth, userService, logEntryService);
    this.formFactory = formFactory;
    this.configuration = configuration;
    this.localeService = localeService;
    this.localeApiService = localeApiService;
    this.projectService = projectService;
  }

  public Result locale(UUID localeId) {
    return locale(localeId, locale -> {
      Form<KeySearchForm> form = FormUtils.KeySearch.bindFromRequest(formFactory, configuration);
      KeySearchForm search = form.get();
      if (search.order == null)
        search.order = "name";

      return ok(views.html.locales.locale.render(createTemplate(), locale, form));
    });
  }

  public Result doCreate(UUID projectId) {
    Project project = projectService.byId(projectId);
    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    Form<LocaleForm> form = formFactory.form(LocaleForm.class).bindFromRequest();

    if (form.hasErrors())
      return badRequest(views.html.locales.create.render(createTemplate(), project, form));

    LOGGER.debug("Locale: {}", Json.toJson(form));

    Locale locale = form.get().into(new Locale());

    locale.project = project;

    localeService.save(locale);

    try {
      localeApiService.upload(locale.id, request());
    } catch (IllegalStateException e) {
      // This is OK, the fileType doesn't need to be filled
    }

    return redirect(routes.Locales.locale(locale.id));
  }

  public Result createImmediately(UUID projectId, String localeName) {
    Project project = projectService.byId(projectId);

    if (project == null)
      return redirect(routes.Application.index());

    select(project);

    if (localeName.length() > Locale.NAME_LENGTH)
      return badRequest(views.html.locales.create.render(createTemplate(), project,
          formFactory.form(LocaleForm.class).bind(ImmutableMap.of("name", localeName))));

    Locale locale = Locale.byProjectAndName(project, localeName);

    if (locale == null) {
      locale = new Locale(project, localeName);

      LOGGER.debug("Locale: {}", Json.toJson(locale));

      localeService.save(locale);
    }

    return redirect(routes.Locales.locale(locale.id));
  }

  public Result edit(UUID localeId) {
    return locale(localeId, locale -> {
      return ok(views.html.locales.edit.render(createTemplate(), locale,
          formFactory.form(LocaleForm.class).fill(LocaleForm.from(locale))));
    });
  }

  public Result doEdit(UUID localeId) {
    return locale(localeId, locale -> {
      Form<LocaleForm> form = formFactory.form(LocaleForm.class).bindFromRequest();

      if (form.hasErrors())
        return badRequest(views.html.locales.edit.render(createTemplate(), locale, form));

      localeService.save(form.get().into(locale));

      return redirect(routes.Projects.locales(locale.project.id, DEFAULT_SEARCH, DEFAULT_ORDER,
          DEFAULT_LIMIT, DEFAULT_OFFSET));
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

      return redirect(routes.Locales.locale(localeId));
    });
  }

  public Result remove(UUID localeId, String s, String order, int limit, int offset) {
    return locale(localeId, locale -> {
      undoCommand(injector.instanceOf(RevertDeleteLocaleCommand.class).with(locale));

      try {
        TransactionUtils.batchExecute((tx) -> {
          localeService.delete(locale);
        });
      } catch (Exception e) {
        LOGGER.error("Error while batch deleting locale", e);
      }

      LOGGER.debug("Redirecting");

      return redirect(routes.Projects.locales(locale.project.id, s, order, limit, offset));
    });
  }

  public Result translate(UUID localeId) {
    return locale(localeId, locale -> {
      String referer = request().getHeader("Referer");

      if (referer == null)
        return redirect(routes.Locales.locale(localeId));

      return redirect(referer);
    });
  }

  private Result locale(UUID localeId, Function<Locale, Result> processor) {
    Locale locale = localeService.byId(localeId);
    if (locale == null)
      return redirect(routes.Dashboards.dashboard());

    select(locale.project);

    return processor.apply(locale);
  }
}
