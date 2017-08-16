package controllers;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;
import commands.RevertDeleteLocaleCommand;
import criterias.LocaleCriteria;
import forms.KeySearchForm;
import forms.LocaleForm;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import models.Locale;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.data.Form;
import play.data.FormFactory;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.With;
import repositories.ProjectRepository;
import services.CacheService;
import services.LocaleService;
import services.api.LocaleApiService;
import utils.FormUtils;
import utils.Template;
import utils.TransactionUtils;

/**
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

  @Inject
  protected Locales(Injector injector, CacheService cache, PlayAuthenticate auth,
      FormFactory formFactory, Configuration configuration, LocaleService localeService,
      LocaleApiService localeApiService) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.configuration = configuration;
    this.localeService = localeService;
    this.localeApiService = localeApiService;
  }

  public CompletionStage<Result> localeBy(String username, String projectName, String localeName,
      String search, String order, int limit, int offset) {
    return locale(username, projectName, localeName, (project, locale) -> {
      Form<KeySearchForm> form =
          FormUtils.KeySearch.bindFromRequest(formFactory, configuration);
      form.get().update(search, order, limit, offset);

      return ok(views.html.locales.locale.render(createTemplate(), locale, form));
    }, ProjectRepository.FETCH_LOCALES);
  }

  public CompletionStage<Result> doCreateBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      Form<LocaleForm> form = FormUtils.Locale.bindFromRequest(formFactory, configuration);

      if (form.hasErrors()) {
        return badRequest(views.html.locales.create.render(createTemplate(), project, form));
      }

      Locale locale = form.get().into(new Locale());

      locale.project = project;

      try {
        localeService.save(locale);
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.locales.create
                .render(createTemplate(), project, FormUtils.include(form, e)));
      }

      try {
        localeApiService.upload(locale.id, request());
      } catch (Exception e) {
        // This is OK, the fileType doesn't need to be filled
      }

      LocaleForm search = form.get();

      return redirect(locale.route(search.search, search.order, search.limit, search.offset));
    });
  }

  public CompletionStage<Result> createImmediatelyBy(String username, String projectName,
      String localeName, String search, String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<LocaleForm> form =
          LocaleForm.with(localeName, FormUtils.Locale.bindFromRequest(formFactory, configuration));
      if (localeName.length() > Locale.NAME_LENGTH) {
        return badRequest(views.html.locales.create.render(createTemplate(), project, form));
      }

      Locale locale = localeService.byProjectAndName(project, localeName);

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

      return redirect(locale.route(search, order, limit, offset));
    });
  }

  public CompletionStage<Result> editBy(String username, String projectName, String localeName,
      String search, String order, int limit, int offset) {
    return locale(username, projectName, localeName,
        (project, locale) -> ok(views.html.locales.edit.render(createTemplate(), locale,
            LocaleForm
                .with(locale, FormUtils.Locale.bindFromRequest(formFactory, configuration)))));
  }

  public CompletionStage<Result> doEditBy(String username, String projectName, String localeName) {
    return locale(username, projectName, localeName, (project, locale) -> {
      Form<LocaleForm> form = FormUtils.Locale.bindFromRequest(formFactory, configuration);

      if (form.hasErrors()) {
        return badRequest(views.html.locales.edit.render(createTemplate(), locale, form));
      }

      localeService.update(form.get().into(locale));

      LocaleForm search = form.get();
      return redirect(
          routes.Projects.localesBy(locale.project.owner.username, locale.project.name,
              search.search, search.order, search.limit, search.offset));
    });
  }

  public CompletionStage<Result> uploadBy(String username, String projectName, String localeName) {
    return locale(username, projectName, localeName, (project, locale) ->
        ok(views.html.locales.upload.render(createTemplate(), locale.project, locale))
    );
  }

  public CompletionStage<Result> doUploadBy(String username, String projectName,
      String localeName) {
    return locale(username, projectName, localeName, (project, locale) -> {
      try {
        localeApiService.upload(locale.id, request());
      } catch (Exception e) {
        addError(e.getMessage());
        return badRequest(
            views.html.locales.upload.render(createTemplate(), locale.project, locale));
      }

      return redirect(locale.route());
    });
  }

  public CompletionStage<Result> removeBy(String username, String projectName, String localeName,
      String s, String order, int limit, int offset) {
    return locale(username, projectName, localeName, (project, locale) -> {
      undoCommand(injector.instanceOf(RevertDeleteLocaleCommand.class).with(locale));

      try {
        TransactionUtils.batchExecute((tx) -> localeService.delete(locale));
      } catch (Exception e) {
        LOGGER.error("Error while batch deleting locale", e);
      }

      return redirect(
          routes.Projects.localesBy(locale.project.owner.username, locale.project.name,
              s, order, limit, offset));
    });
  }

  private Result locale(Project project, String localeName, Function<Locale, Result> processor) {
    if (project.locales != null) {
      Optional<Locale> locale =
          project.locales.stream().filter(l -> l.name.equals(localeName)).findFirst();
      if (locale.isPresent()) {
        return processor.apply(locale.get());
      }
    }

    PagedList<Locale> locales = localeService
        .findBy(new LocaleCriteria().withProjectId(project.id).withLocaleName(localeName));
    if (locales.getList().isEmpty()) {
      return redirect(project.route());
    }

    select(project);

    return processor.apply(locales.getList().get(0));
  }

  private CompletionStage<Result> locale(String username, String projectName, String localeName,
      BiFunction<Project, Locale, Result> processor, String... fetches) {
    return project(username, projectName,
        (user, project) -> locale(project, localeName, locale -> processor.apply(project, locale)),
        fetches);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_PROJECTS);
  }

  public static Call doCreateRoute(Project project) {
    return routes.Locales.doCreateBy(project.owner.username, project.name);
  }

  public static Call createImmediatelyRoute(dto.Project project, String localeName,
      String search, String order, int limit, int offset) {
    return routes.Locales
        .createImmediatelyBy(project.ownerUsername, project.name, localeName, search, order, limit,
            offset);
  }
}
