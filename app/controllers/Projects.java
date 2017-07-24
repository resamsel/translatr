package controllers;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;
import commands.RevertDeleteProjectCommand;
import commands.RevertDeleteProjectUserCommand;
import converters.ActivityCsvConverter;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.ProjectUserCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.ActivitySearchForm;
import forms.KeySearchForm;
import forms.LocaleSearchForm;
import forms.ProjectForm;
import forms.ProjectOwnerForm;
import forms.ProjectUserForm;
import forms.SearchForm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import models.Key;
import models.Locale;
import models.LogEntry;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Suggestable;
import models.Suggestable.Data;
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
import services.ProjectService;
import services.ProjectUserService;
import utils.FormUtils;
import utils.PermissionUtils;
import utils.Template;

/**
 * @author resamsel
 * @version 16 Sep 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Projects extends AbstractController {

  private final ProjectService projectService;

  private final LocaleService localeService;

  private final KeyService keyService;

  private final FormFactory formFactory;

  private final ProjectUserService projectUserService;

  private final Configuration configuration;

  /**
   *
   */
  @Inject
  public Projects(Injector injector, CacheApi cache, FormFactory formFactory, PlayAuthenticate auth,
      ProjectService projectService, LocaleService localeService, KeyService keyService,
      ProjectUserService projectUserService, Configuration configuration) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.projectService = projectService;
    this.localeService = localeService;
    this.keyService = keyService;
    this.projectUserService = projectUserService;
    this.configuration = configuration;
  }

  public CompletionStage<Result> index(String s, String order, int limit, int offset) {
    return loggedInUser(user -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Project> projects =
          projectService.findBy(ProjectCriteria.from(search).withMemberId(User.loggedInUserId()));

      search.pager(projects);

      return ok(views.html.projects.index.render(createTemplate(), projects, form,
          ProjectForm.form(formFactory)));
    });
  }

  @SubjectPresent
  public Result search() {
    Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
    SearchForm search = form.get();

    List<Suggestable> suggestions = new ArrayList<>();

    PagedList<? extends Suggestable> projects = projectService.findBy(ProjectCriteria.from(search));
    if (!projects.getList().isEmpty()) {
      suggestions.addAll(projects.getList());
    } else {
      suggestions.add(Suggestable.DefaultSuggestable
          .from(ctx().messages().at("project.create", search.search), Data.from(Project.class, null,
              "+++", controllers.routes.Projects.createImmediately(search.search).url())));
    }

    return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
  }

  public CompletionStage<Result> projectBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.values())) {
        return redirectWithError(user.route(), "project.access.denied", project.name);
      }

      return ok(views.html.projects.project.render(createTemplate(), project,
          FormUtils.Search.bindFromRequest(formFactory, configuration)));
    }, User.FETCH_PROJECTS);
  }

  public CompletionStage<Result> createBy(String username) {
    return user(username, user -> ok(views.html.projects.create
        .render(createTemplate(), ProjectForm.form(formFactory).bindFromRequest())));
  }

  public CompletionStage<Result> doCreateBy(String username) {
    return user(username, user -> {
      Form<ProjectForm> form = ProjectForm.form(formFactory).bindFromRequest();
      if (form.hasErrors()) {
        throw new ConstraintViolationException(Collections.emptySet());
      }

      Project project = Project.byOwnerAndName(user.username, form.get().getName());
      if (project != null) {
        form.get().fill(project).withDeleted(false);
      } else {
        project = form.get().fill(new Project()).withOwner(user);
      }

      try {
        project = projectService.save(project);
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.projects.create.render(createTemplate(), FormUtils.include(form, e)));
      }

      addMessage(message("project.created", project.name));

      select(project);

      return redirect(project.route());
    });
  }

  public CompletionStage<Result> createImmediately(String projectName) {
    return loggedInUser(user -> {
      Form<ProjectForm> form =
          ProjectForm.form(formFactory).bind(ImmutableMap.of("name", projectName));
      if (form.hasErrors()) {
        return badRequest(views.html.projects.create.render(createTemplate(), form));
      }

      Project project = Project.byOwnerAndName(user.username, projectName);
      if (project == null) {
        try {
          project = projectService.save(new Project(projectName).withOwner(user));
        } catch (ConstraintViolationException e) {
          return badRequest(
              views.html.projects.create.render(createTemplate(), FormUtils
                  .include(form, e)));
        }

        addMessage(message("project.created", project.name));
      }

      select(project);

      return redirect(project.route());
    });
  }

  public CompletionStage<Result> editBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.route(), "project.edit.denied", project.name);
      }

      return ok(views.html.projects.edit.render(createTemplate(), project,
          formFactory.form(ProjectForm.class).fill(ProjectForm.from(project))));
    });
  }

  public CompletionStage<Result> doEditBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.route(), "project.edit.denied", project.name);
      }

      Form<ProjectForm> form = formFactory.form(ProjectForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return badRequest(views.html.projects.edit.render(createTemplate(), project, form));
      }

      try {
        project = projectService.save(form.get().fill(project));
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.projects.edit.render(createTemplate(), project, FormUtils.include(form, e)));
      }

      return redirect(project.route());
    });
  }

  public CompletionStage<Result> removeBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.route(), "project.delete.denied", project.name);
      }

      select(project);

      undoCommand(RevertDeleteProjectCommand.from(project));

      projectService.delete(project);

      return redirect(user.route());
    });
  }

  public CompletionStage<Result> localesBy(String username, String projectName, String s,
      String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<LocaleSearchForm> form =
          FormUtils.LocaleSearch.bindFromRequest(formFactory, configuration);
      LocaleSearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Locale> locales =
          Locale.findBy(LocaleCriteria.from(search).withProjectId(project.id));

      search.pager(locales);

      return ok(views.html.projects.locales.render(createTemplate(), project, locales,
          localeService.progress(
              locales.getList().stream().map(l -> l.id).collect(Collectors.toList()),
              project.keys.size()),
          form));
    });
  }

  public CompletionStage<Result> keysBy(String username, String projectName, String s, String order,
      int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<KeySearchForm> form = FormUtils.KeySearch.bindFromRequest(formFactory, configuration);
      KeySearchForm search = form.get();
      search.update(s, order, limit, offset);

      PagedList<Key> keys = Key.findBy(KeyCriteria.from(search).withProjectId(project.id));

      search.pager(keys);

      Map<UUID, Double> progress =
          keyService.progress(keys.getList().stream().map(k -> k.id).collect(Collectors.toList()),
              project.locales.size());

      return ok(views.html.projects.keys.render(createTemplate(), project, keys, progress, form));
    });
  }

  public CompletionStage<Result> membersBy(String username, String projectName, String s,
      String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();

      PagedList<ProjectUser> list =
          ProjectUser.findBy(ProjectUserCriteria.from(search).withProjectId(project.id));

      search.pager(list);

      return ok(views.html.projects.members.render(createTemplate(), project, list, form,
          ProjectUserForm.form(formFactory)));
    });
  }

  public CompletionStage<Result> memberAddBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> ok(views.html.projects.memberAdd
        .render(createTemplate(), project, ProjectUserForm.form(formFactory).bindFromRequest())));
  }

  public CompletionStage<Result> doMemberAddBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      Form<ProjectUserForm> form = ProjectUserForm.form(formFactory).bindFromRequest();

      if (form.hasErrors()) {
        return badRequest(views.html.projects.memberAdd.render(createTemplate(), project, form));
      }

      User member = userService.byUsername(form.get().getUsername());

      projectUserService
          .save(form.get().fill(new ProjectUser()).withProject(project).withUser(member));

      return redirect(project.membersRoute());
    });
  }

  public CompletionStage<Result> memberRemoveBy(String username, String projectName,
      Long memberId) {
    return project(username, projectName, (user, project) -> {
      ProjectUser member = projectUserService.byId(memberId);

      if (member == null || !project.id.equals(member.project.id)) {
        return redirectWithError(project.membersRoute(), "project.member.notFound");
      }

      undoCommand(RevertDeleteProjectUserCommand.from(member));

      projectUserService.delete(member);

      return redirect(project.membersRoute());
    });
  }

  public CompletionStage<Result> doOwnerChangeBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      Form<ProjectOwnerForm> form = formFactory.form(ProjectOwnerForm.class).bindFromRequest();

      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.membersRoute(), "project.owner.change.denied",
            project.name);
      }

      // if (form.hasErrors())
      // return badRequest(views.html.projects.ownerChange.render(createTemplate(), project, form));

      ProjectOwnerForm val = form.get();
      // Make old owner a member of type Manager
      project.members.stream().filter(m -> m.role == ProjectRole.Owner)
          .forEach(m -> m.role = ProjectRole.Manager);
      // Make new owner a member of type Owner
      project.members.stream().filter(m -> m.user.id.equals(val.getOwnerId()))
          .forEach(m -> m.role = ProjectRole.Owner);
      projectService.save(project.withOwner(userService.byId(val.getOwnerId())));

      return redirect(project.membersRoute());
    });
  }

  public CompletionStage<Result> activityBy(String username, String projectName, String s,
      String order, int limit, int offset) {
    return project(username, projectName, (user, project) -> {
      Form<ActivitySearchForm> form =
          FormUtils.ActivitySearch.bindFromRequest(formFactory, configuration);
      ActivitySearchForm search = form.get();

      PagedList<LogEntry> activities = logEntryService.findBy(
          LogEntryCriteria.from(search).withProjectId(project.id).withOrder("whenCreated desc"));

      search.pager(activities);

      return ok(views.html.projects.activity.render(createTemplate(), project, activities, form));
    });
  }

  public CompletionStage<Result> activityCsvBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> ok(new ActivityCsvConverter()
        .apply(logEntryService.getAggregates(new LogEntryCriteria().withProjectId(project.id)))));
  }

  public CompletionStage<Result> wordCountResetBy(String username, String projectName) {
    return project(username, projectName, (user, project) -> {
      if (!PermissionUtils.hasPermissionAny(project, ProjectRole.Owner, ProjectRole.Manager)) {
        return redirectWithError(project.route(), "project.edit.denied", project.name);
      }

      select(project);

      projectService.resetWordCount(project.id);

      return redirectWithMessage(project.route(), "project.wordCount.reset");
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_PROJECTS);
  }

  /**
   * @return
   */
  public static Call indexRoute() {
    return routes.Projects.index(DEFAULT_SEARCH, DEFAULT_ORDER, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * @return
   * @param username
   */
  public static Call createRoute(String username) {
    return routes.Projects.createBy(username);
  }

  /**
   * @return
   * @param username
   */
  public static Call doCreateRoute(String username) {
    return routes.Projects.doCreateBy(username);
  }
}
