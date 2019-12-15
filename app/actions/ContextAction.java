package actions;

import commands.Command;
import controllers.AbstractController;
import controllers.routes;
import models.User;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import services.AuthProvider;
import services.CacheService;
import utils.ContextKey;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author resamsel
 * @version 17 Aug 2016
 */
public class ContextAction extends Action.Simple {

  private static final List<String> ALLOWED_ROUTES =
      Arrays.asList(routes.Application.logout().path(), routes.Profiles.edit().path());

  private final CacheService cache;
  private final AuthProvider authProvider;

  /**
   *
   */
  @Inject
  public ContextAction(CacheService cache, AuthProvider authProvider) {
    this.cache = cache;
    this.authProvider = authProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CompletionStage<Result> call(Context ctx) {
    // DEBUG
    // AbstractController.addMessage(ctx.messages().at("user.incomplete"));

    User user = authProvider.loggedInUser();
    if (user != null && !user.isComplete() && !routeAllowed(ctx.request().path())) {
      AbstractController.addMessage(ctx.messages().at("user.incomplete"));
      return CompletableFuture.completedFuture(redirect(routes.Profiles.edit()));
    }

    if (ctx.flash().containsKey("undo")) {
      String key = ctx.flash().get("undo");
      Command<?> command = (Command<?>) cache.get(key);
      if (command != null) {
        ContextKey.UndoMessage.put(ctx, command.getMessage());
        ContextKey.UndoCommand.put(ctx, key);
      }
    }

    return delegate.call(ctx);
  }

  /**
   * @param path
   * @return
   */
  private boolean routeAllowed(String path) {
    return ALLOWED_ROUTES.contains(path);
  }
}
