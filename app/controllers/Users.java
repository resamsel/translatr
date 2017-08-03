package controllers;

import static java.util.stream.Collectors.toMap;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;
import commands.RevertDeleteAccessTokenCommand;
import commands.RevertDeleteLinkedAccountCommand;
import converters.ActivityCsvConverter;
import criterias.AccessTokenCriteria;
import criterias.LinkedAccountCriteria;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import criterias.UserCriteria;
import forms.AccessTokenForm;
import forms.ActivitySearchForm;
import forms.SearchForm;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import models.AccessToken;
import models.LinkedAccount;
import models.LogEntry;
import models.Project;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.AccessTokenService;
import services.LinkedAccountService;
import services.ProjectService;
import utils.FormUtils;
import utils.Template;

/**
 * @author resamsel
 * @version 26 Sep 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Users extends AbstractController {

  private final FormFactory formFactory;

  private final Configuration configuration;

  private final ProjectService projectService;

  private final LinkedAccountService linkedAccountService;

  private final AccessTokenService accessTokenService;

  @Inject
  public Users(Injector injector, CacheApi cache, PlayAuthenticate auth, FormFactory formFactory,
      Configuration configuration, ProjectService projectService,
      LinkedAccountService linkedAccountService, AccessTokenService accessTokenService) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.configuration = configuration;
    this.projectService = projectService;
    this.linkedAccountService = linkedAccountService;
    this.accessTokenService = accessTokenService;
  }

  public CompletionStage<Result> index() {
    return tryCatch(() -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      if (search.order == null) {
        search.order = "name";
      }

      PagedList<User> users = userService.findBy(UserCriteria.from(search)
          .withFetches(User.FETCH_MEMBERSHIPS).withFetches(User.FETCH_ACTIVITIES));

      search.pager(users);

      return ok(views.html.users.index.render(createTemplate().withSection(SECTION_COMMUNITY),
          users, form));
    });
  }

  public CompletionStage<Result> user(String username) {
    return user(username, user -> ok(views.html.users.user.render(createTemplate(user), user,
        userService.getUserStats(user.id))));
  }

  public CompletionStage<Result> projects(String username) {
    return user(username, user -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      if (search.order == null) {
        search.order = "name";
      }

      PagedList<Project> projects =
          projectService.findBy(ProjectCriteria.from(search).withMemberId(User.loggedInUserId()));

      search.pager(projects);

      return ok(views.html.users.projects.render(createTemplate(user), user, projects, form));
    }, true);
  }

  public CompletionStage<Result> activity(String username) {
    return user(username, user -> {
      Form<ActivitySearchForm> form =
          FormUtils.ActivitySearch.bindFromRequest(formFactory, configuration);
      ActivitySearchForm search = form.get();

      PagedList<LogEntry> activities =
          logEntryService.findBy(LogEntryCriteria.from(search).withUserId(user.id));

      search.pager(activities);

      return ok(views.html.users.activity.render(createTemplate(user), user, activities, form));
    }, true);
  }

  public CompletionStage<Result> activityCsv(String username) {
    return user(username, user -> ok(new ActivityCsvConverter()
        .apply(logEntryService.getAggregates(new LogEntryCriteria().withUserId(user.id)))), true);
  }

  public CompletionStage<Result> linkedAccounts(String username) {
    return user(username, user -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();

      PagedList<LinkedAccount> accounts = LinkedAccount
          .findBy(LinkedAccountCriteria.from(search).withUserId(user.id).withOrder("providerKey"));

      search.pager(accounts);

      return ok(views.html.users.linkedAccounts.render(createTemplate(user), user,
          accounts.getList().stream().collect(toMap(a -> a.providerKey, a -> a)), form));
    }, true);
  }

  public CompletionStage<Result> linkedAccountRemove(String username, Long linkedAccountId) {
    return user(username, user -> {
      LinkedAccount linkedAccount = linkedAccountService.byId(linkedAccountId);

      if (linkedAccount == null || !user.id.equals(linkedAccount.user.id)) {
        return redirect(routes.Users.linkedAccounts(user.username));
      }

      undoCommand(RevertDeleteLinkedAccountCommand.from(linkedAccount));

      linkedAccountService.delete(linkedAccount);

      return redirect(routes.Users.linkedAccounts(user.username));
    }, true);
  }

  public CompletionStage<Result> accessTokens(String username) {
    return user(username, user -> {
      Form<AccessTokenForm> form =
          FormUtils.AccessToken.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      if (search.order == null) {
        search.order = "name";
      }

      return ok(views.html.users.accessTokens.render(createTemplate(user), user,
          accessTokenService.findBy(AccessTokenCriteria.from(search).withUserId(user.id)).getList(),
          form));
    }, true);
  }

  public CompletionStage<Result> accessTokenEdit(String username, Long accessTokenId) {
    return user(username, user -> {
      AccessToken accessToken = accessTokenService.byId(accessTokenId);
      if (accessToken == null) {
        return redirectWithError(routes.Users.accessTokens(user.username), "accessToken.notFound");
      }

      if (!accessToken.user.id.equals(user.id)) {
        addError(ctx().messages().at("accessToken.access.denied", accessToken.id));
        return redirect(routes.Users.accessTokens(user.username));
      }

      return ok(views.html.users.accessToken.render(createTemplate(), user, accessToken,
          AccessTokenForm.form(formFactory).fill(AccessTokenForm.from(accessToken))));
    });
  }

  public CompletionStage<Result> doAccessTokenEdit(String username, Long accessTokenId) {
    return user(username, user -> {
      AccessToken accessToken = accessTokenService.byId(accessTokenId);
      if (accessToken == null) {
        return redirectWithError(routes.Users.accessTokens(user.username), "accessToken.notFound");
      }

      Form<AccessTokenForm> form = AccessTokenForm.form(formFactory).bindFromRequest();

      if (form.hasErrors()) {
        return badRequest(
            views.html.users.accessToken.render(createTemplate(), user, accessToken, form));
      }

      try {
        accessTokenService.save(form.get().fill(accessToken));
      } catch (ConstraintViolationException e) {
        return badRequest(views.html.users.accessToken.render(createTemplate(), user, accessToken,
            FormUtils.include(form, e)));
      }

      return redirect(routes.Users.accessTokens(user.username));
    });
  }

  public CompletionStage<Result> accessTokenRemove(String username, Long accessTokenId) {
    return user(username, user -> {
      AccessToken accessToken = accessTokenService.byId(accessTokenId);

      if (accessToken == null || !user.id.equals(accessToken.user.id)) {
        return redirectWithError(routes.Users.accessTokens(user.username),
            "accessToken.notAllowed");
      }

      undoCommand(RevertDeleteAccessTokenCommand.from(accessToken));

      accessTokenService.delete(accessToken);

      return redirect(routes.Users.accessTokens(user.username));
    }, true);
  }

  private Template createTemplate(User user) {
    Template template = createTemplate();

    if (user != null && template.loggedInUser != null && user.id.equals(template.loggedInUser.id)) {
      return template.withSection(SECTION_PROFILE);
    }

    return template.withSection(SECTION_COMMUNITY);
  }
}
