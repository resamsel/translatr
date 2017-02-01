package security;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import controllers.AbstractController;
import play.mvc.Http;
import play.mvc.Http.Session;
import play.mvc.Result;
import services.UserService;
import utils.Template;

public class MyDeadboltHandler extends AbstractDeadboltHandler {
  private final PlayAuthenticate auth;

  private final UserService userService;

  public MyDeadboltHandler(final PlayAuthenticate auth,
      final ExecutionContextProvider exContextProvider, final UserService userService) {
    super(exContextProvider);
    this.auth = auth;
    this.userService = userService;
  }

  @Override
  public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context) {
    if (auth.isLoggedIn(context.session())) {
      // user is logged in
      return CompletableFuture.completedFuture(Optional.empty());
    } else {
      // user is not logged in

      // call this if you want to redirect your visitor to the page that
      // was requested before sending him to the login page
      // if you don't call this, the user will get redirected to the page
      // defined by your resolver
      final String originalUrl = auth.storeOriginalUrl(context);

      // Re-try login, if possible
      Session session = context.session();
      if (session.containsKey("pa.p.id") && auth.getProvider(session.get("pa.p.id")) != null)
        return CompletableFuture.completedFuture(Optional.ofNullable(
            AbstractController.redirect(auth.getResolver().auth(session.get("pa.p.id")))));

      return CompletableFuture.completedFuture(
          Optional.ofNullable(AbstractController.redirectWithError(auth.getResolver().login(),
              "You need to log in first to view '" + originalUrl + "'")));
    }
  }

  @Override
  public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context) {
    final AuthUserIdentity u = auth.getUser(context);

    return CompletableFuture.completedFuture(Optional.ofNullable(userService.getLocalUser(u)));
  }

  @Override
  public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(
      final Http.Context context) {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletionStage<Result> onAuthFailure(final Http.Context context,
      final Optional<String> content) {
    // if the user has a cookie with a valid user and the local user has
    // been deactivated/deleted in between, it is possible that this gets
    // shown. You might want to consider to sign the user out in this case.
    return CompletableFuture.completedFuture(
        forbidden(views.html.errors.restricted.render(Template.create(auth, null))));
  }
}
