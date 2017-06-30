package controllers;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.joda.time.DateTime;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import commands.RevertDeleteAccessTokenCommand;
import forms.Accept;
import forms.AccessTokenForm;
import forms.UserForm;
import models.AccessToken;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.AccessTokenService;
import utils.FormUtils;
import utils.SessionKey;
import utils.Template;

/**
 *
 * @author resamsel
 * @version 6 Oct 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Profiles extends AbstractController {
  private final FormFactory formFactory;

  private final AccessTokenService accessTokenService;

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   */
  @Inject
  public Profiles(Injector injector, CacheApi cache, PlayAuthenticate auth, FormFactory formFactory,
      AccessTokenService accessTokenService) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.accessTokenService = accessTokenService;
  }

  public CompletionStage<Result> profile() {
    return loggedInUser(user -> redirect(controllers.routes.Users.user(user.username)));
  }

  public CompletionStage<Result> linkedAccounts() {
    return loggedInUser(user -> redirect(controllers.routes.Users.linkedAccounts(user.username)));
  }

  public CompletionStage<Result> accessTokens() {
    return loggedInUser(user -> redirect(controllers.routes.Users.accessTokens(user.username)));
  }

  public CompletionStage<Result> activity() {
    return loggedInUser(user -> redirect(controllers.routes.Users.activity(user.username)));
  }

  public CompletionStage<Result> edit() {
    return loggedInUser(user -> {
      return ok(views.html.users.edit.render(createTemplate(), user,
          formFactory.form(UserForm.class).fill(UserForm.from(user))));
    });
  }

  public CompletionStage<Result> doEdit() {
    return loggedInUser(user -> {
      Form<UserForm> form = formFactory.form(UserForm.class).bindFromRequest();

      if (form.hasErrors())
        return badRequest(views.html.users.edit.render(createTemplate(), user, form));

      try {
        userService.save(form.get().into(user));
      } catch (ConstraintViolationException e) {
        return badRequest(
            views.html.users.edit.render(createTemplate(), user, FormUtils.include(form, e)));
      }

      return redirect(routes.Users.user(user.username));
    });
  }

  @SubjectPresent
  public Result askLink() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    final AuthUser newAccount = this.auth.getLinkUser(session());
    if (newAccount == null) {
      // account to link could not be found, silently redirect to login
      return redirect(routes.Application.index());
    }

    return ok(views.html.profiles.askLink.render(createTemplate(), formFactory.form(Accept.class),
        newAccount));
  }

  public Result doLink() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());
    final AuthUser newAccount = auth.getLinkUser(session());
    if (newAccount == null) {
      // account to link could not be found, silently redirect to login
      return redirect(routes.Application.index());
    }

    final Form<Accept> filledForm = formFactory.form(Accept.class).bindFromRequest();
    if (filledForm.hasErrors()) {
      // User did not select whether to link or not link
      return badRequest(
          views.html.profiles.askLink.render(createTemplate(), filledForm, newAccount));
    } else {
      // User made a choice :)
      final boolean link = filledForm.get().accept;
      if (link)
        addMessage(ctx().messages().at("playauthenticate.accounts.link.success"));

      return auth.link(ctx(), link);
    }
  }

  @SubjectPresent
  public Result askMerge() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    // this is the currently logged in user
    final AuthUser aUser = auth.getUser(session());

    // this is the user that was selected for a login
    final AuthUser bUser = auth.getMergeUser(session());
    if (bUser == null)
      // user to merge with could not be found, silently redirect to login
      return redirect(routes.Application.index());

    // You could also get the local user object here via
    // User.findByAuthUserIdentity(newUser)
    return ok(views.html.profiles.askMerge.render(createTemplate(), formFactory.form(Accept.class),
        aUser, bUser));
  }

  @SubjectPresent
  public Result doMerge() {
    com.feth.play.module.pa.controllers.Authenticate.noCache(response());

    // this is the currently logged in user
    final AuthUser aUser = this.auth.getUser(session());

    // this is the user that was selected for a login
    final AuthUser bUser = this.auth.getMergeUser(session());
    if (bUser == null)
      // user to merge with could not be found, silently redirect to login
      return redirect(routes.Application.index());

    final Form<Accept> filledForm = formFactory.form(Accept.class).bindFromRequest();
    if (filledForm.hasErrors()) {
      // User did not select whether to merge or not merge
      return badRequest(
          views.html.profiles.askMerge.render(createTemplate(), filledForm, aUser, bUser));
    } else {
      // User made a choice :)
      final boolean merge = filledForm.get().accept;
      if (merge)
        addMessage(ctx().messages().at("playauthenticate.accounts.merge.success"));

      return auth.merge(ctx(), merge);
    }
  }

  public CompletionStage<Result> accessTokenEdit(Long accessTokenId) {
    return loggedInUser(user -> {
      AccessToken accessToken = accessTokenService.byId(accessTokenId);
      if (accessToken == null)
        return redirectWithError(routes.Users.accessTokens(user.username), "accessToken.notFound");

      if (!accessToken.user.id.equals(user.id)) {
        addError(ctx().messages().at("accessToken.access.denied", accessToken.id));
        return redirect(routes.Users.accessTokens(user.username));
      }

      return ok(views.html.users.accessToken.render(createTemplate(), user, accessToken,
          AccessTokenForm.form(formFactory).fill(AccessTokenForm.from(accessToken))));
    });
  }

  public CompletionStage<Result> doAccessTokenEdit(Long accessTokenId) {
    return loggedInUser(user -> {
      AccessToken accessToken = accessTokenService.byId(accessTokenId);
      if (accessToken == null)
        return redirectWithError(routes.Users.accessTokens(user.username), "accessToken.notFound");

      Form<AccessTokenForm> form = AccessTokenForm.form(formFactory).bindFromRequest();

      if (form.hasErrors())
        return badRequest(
            views.html.users.accessToken.render(createTemplate(), user, accessToken, form));

      try {
        accessTokenService.save(form.get().fill(accessToken));
      } catch (ConstraintViolationException e) {
        return badRequest(views.html.users.accessToken.render(createTemplate(), user, accessToken,
            FormUtils.include(form, e)));
      }

      return redirect(routes.Users.accessTokens(user.username));
    });
  }

  public CompletionStage<Result> accessTokenCreate() {
    return loggedInUser(user -> {
      return ok(views.html.users.accessTokenCreate.render(createTemplate(), user,
          AccessTokenForm.form(formFactory).bindFromRequest()));
    });
  }

  public CompletionStage<Result> doAccessTokenCreate() {
    return loggedInUser(user -> {
      Form<AccessTokenForm> form = AccessTokenForm.form(formFactory).bindFromRequest();

      if (form.hasErrors())
        return badRequest(views.html.users.accessTokenCreate.render(createTemplate(), user, form));

      AccessToken accessToken;
      try {
        accessToken = accessTokenService.save(form.get().fill(new AccessToken()).withUser(user));
      } catch (ConstraintViolationException e) {
        return badRequest(views.html.users.accessTokenCreate.render(createTemplate(), user,
            FormUtils.include(form, e)));
      }

      return redirect(routes.Profiles.accessTokenEdit(accessToken.id));
    });
  }

  public CompletionStage<Result> accessTokenRemove(Long accessTokenId) {
    return loggedInUser(user -> {
      AccessToken accessToken = accessTokenService.byId(accessTokenId);

      if (accessToken == null || !user.id.equals(accessToken.user.id))
        return redirectWithError(routes.Users.accessTokens(user.username),
            "accessToken.notAllowed");

      undoCommand(RevertDeleteAccessTokenCommand.from(accessToken));

      accessTokenService.delete(accessToken);

      return redirect(routes.Users.accessTokens(user.username));
    });
  }

  public Result resetNotifications() {
    session(SessionKey.LastAcknowledged.key(), DateTime.now().toString());
    return ok();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Template createTemplate() {
    return super.createTemplate().withSection(SECTION_PROFILE);
  }
}
