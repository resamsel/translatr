package controllers;

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
import play.mvc.Call;
import play.mvc.Result;
import play.mvc.With;
import services.LocaleService;
import services.ProjectService;
import services.api.LocaleApiService;
import utils.FormUtils;
import utils.Template;
import utils.TransactionUtils;
import utils.UrlUtils;

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

  public CompletionStage<Result> localeBy(String username, String projectName, String localeName,
      String search, String order, int limit, int offset) {
    return user(username, user -> project(user, projectName,
        project -> locale(project, UrlUtils.decode(localeName), locale -> {
          Form<KeySearchForm> form =
              FormUtils.KeySearch.bindFromRequest(formFactory, configuration);
          form.get().update(search, order, limit, offset);

          return ok(views.html.locales.locale.render(createTemplate(), locale, form));
        })), User.FETCH_PROJECTS, User.FETCH_PROJECTS + ".locales");
  }

  public CompletionStage<Result> doCreateBy(String username, String projectName) {
    return user(username, user -> project(user, projectName, project -> {
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
    }));
  }

  public CompletionStage<Result> createImmediatelyBy(String username, String projectName,
      String localeName, String search, String order, int limit, int offset) {
    return user(username, user -> project(user, projectName, project -> {
      Form<LocaleForm> form =
          LocaleForm.with(localeName, FormUtils.Locale.bindFromRequest(formFactory, configuration));
      if (localeName.length() > Locale.NAME_LENGTH) {
        return badRequest(views.html.locales.create.render(createTemplate(), project, form));
      }

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

      return redirect(locale.route(search, order, limit, offset));
    }));
  }

  public CompletionStage<Result> editBy(String username, String projectName, String localeName,
      String search, String order, int limit, int offset) {
    return user(username,
        user -> project(user, projectName, project -> locale(project, localeName, locale -> {
          return ok(views.html.locales.edit.render(createTemplate(), locale,
              LocaleForm
                  .with(locale, FormUtils.Locale.bindFromRequest(formFactory, configuration))));
        })));
  }

  public CompletionStage<Result> doEditBy(String username, String projectName, String localeName) {
    return user(username,
        user -> project(user, projectName, project -> locale(project, localeName, locale -> {
          Form<LocaleForm> form = FormUtils.Locale.bindFromRequest(formFactory, configuration);

          if (form.hasErrors()) {
            return badRequest(views.html.locales.edit.render(createTemplate(), locale, form));
          }

          localeService.save(form.get().into(locale));

          LocaleForm search = form.get();
          return redirect(
              routes.Projects.localesBy(locale.project.owner.username, locale.project.name,
                  search.search, search.order, search.limit, search.offset));
        })));
  }

  public CompletionStage<Result> uploadBy(String username, String projectName, String localeName) {
    return user(username,
        user -> project(user, projectName, project -> locale(project, localeName, locale -> {
          return ok(views.html.locales.upload.render(createTemplate(), locale.project, locale));
        })));
  }

  public CompletionStage<Result> doUploadBy(String username, String projectName,
      String localeName) {
    return user(username,
        user -> project(user, projectName, project -> locale(project, localeName, locale -> {
          try {
            localeApiService.upload(locale.id, request());
          } catch (Exception e) {
            addError(e.getMessage());
            return badRequest(
                views.html.locales.upload.render(createTemplate(), locale.project, locale));
          }

          return redirect(locale.route());
        })));
  }

  public CompletionStage<Result> removeBy(String username, String projectName, String localeName,
      String s, String order, int limit, int offset) {
    return user(username,
        user -> project(user, projectName, project -> locale(project, localeName, locale -> {
          undoCommand(RevertDeleteLocaleCommand.from(locale));

          try {
            TransactionUtils.batchExecute((tx) -> {
              localeService.delete(locale);
            });
          } catch (Exception e) {
            LOGGER.error("Error while batch deleting locale", e);
          }

          return redirect(
              routes.Projects.localesBy(locale.project.owner.username, locale.project.name,
                  s, order, limit, offset));
        })));
  }

  private Result project(User user, String projectName, Function<Project, Result> processor) {
    if (user.projects != null) {
      Optional<Project> project =
          user.projects.stream().filter(p -> p.name.equals(projectName)).findFirst();
      if (project.isPresent()) {
        return processor.apply(project.get());
      }
    }

    Project project = projectService.byOwnerAndName(user, projectName);
    if (project == null) {
      return redirectWithError(Projects.indexRoute(), "project.notFound");
    }

    return processor.apply(project);
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

  private Result locale(UUID localeId, Function<Locale, Result> processor) {
    Locale locale = localeService.byId(localeId);
    if (locale == null) {
      return redirect(Projects.indexRoute());
    }

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

  /**
   * @param project
   * @return
   */
  public static Call doCreateRoute(Project project) {
    return routes.Locales.doCreateBy(project.owner.username, project.name);
  }

  /**
   * @return
   */
  public static Call createImmediatelyRoute(dto.Project project, String localeName,
      String search, String order, int limit, int offset) {
    return routes.Locales
        .createImmediatelyBy(project.ownerUsername, project.name, localeName, search, order, limit,
            offset);
  }
}
