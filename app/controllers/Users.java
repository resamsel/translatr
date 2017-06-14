package controllers;

import static java.util.stream.Collectors.toMap;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
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
import services.LinkedAccountService;
import utils.FormUtils;
import utils.Template;

/**
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
@With(ContextAction.class)
public class Users extends AbstractController {
  private final FormFactory formFactory;

  private final Configuration configuration;

  private final LinkedAccountService linkedAccountService;

  @Inject
  public Users(Injector injector, CacheApi cache, PlayAuthenticate auth, FormFactory formFactory,
      Configuration configuration, LinkedAccountService linkedAccountService) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.configuration = configuration;
    this.linkedAccountService = linkedAccountService;
  }

  public CompletionStage<Result> index() {
    return tryCatch(() -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      if (search.order == null)
        search.order = "name";

      PagedList<User> users = userService.findBy(UserCriteria.from(search)
          .withFetches(User.FETCH_MEMBERSHIPS).withFetches(User.FETCH_ACTIVITIES));

      search.pager(users);

      return ok(views.html.users.index.render(createTemplate().withSection(SECTION_COMMUNITY),
          users, form));
    });
  }

  public Result userByUsername(String username) {
    return user(username);
  }

  public Result user(String username) {
    return user(username, user -> {
      return ok(views.html.users.user.render(createTemplate(user), user,
          userService.getUserStats(user.id)));
    });
  }

  public Result projects(String username) {
    return user(username, user -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      if (search.order == null)
        search.order = "name";

      PagedList<Project> projects =
          Project.findBy(ProjectCriteria.from(search).withMemberId(user.id));

      search.pager(projects);

      return ok(views.html.users.projects.render(createTemplate(user), user, projects, form));
    }, true);
  }

  public Result activity(String username) {
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

  public Result activityCsv(String username) {
    return user(username, user -> {
      return ok(new ActivityCsvConverter()
          .apply(logEntryService.getAggregates(new LogEntryCriteria().withUserId(user.id))));
    }, true);
  }

  public Result linkedAccounts(String username) {
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

  public Result linkedAccountRemove(String username, Long linkedAccountId) {
    return user(username, user -> {
      LinkedAccount linkedAccount = linkedAccountService.byId(linkedAccountId);

      if (linkedAccount == null || !user.id.equals(linkedAccount.user.id))
        return redirect(routes.Users.linkedAccounts(user.username));

      undoCommand(RevertDeleteLinkedAccountCommand.from(linkedAccount));

      linkedAccountService.delete(linkedAccount);

      return redirect(routes.Users.linkedAccounts(user.username));
    }, true);
  }

  public Result accessTokens(String username) {
    return user(username, user -> {
      Form<AccessTokenForm> form =
          FormUtils.AccessToken.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      if (search.order == null)
        search.order = "name";

      return ok(views.html.users.accessTokens.render(createTemplate(user), user,
          AccessToken.findBy(AccessTokenCriteria.from(search).withUserId(user.id)).getList(),
          form));
    }, true);
  }

  private Template createTemplate(User user) {
    Template template = createTemplate();

    if (user != null && template.loggedInUser != null && user.id.equals(template.loggedInUser.id))
      return template.withSection(SECTION_PROFILE);

    return template.withSection(SECTION_COMMUNITY);
  }
}
