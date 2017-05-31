package controllers;

import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import converters.ActivityCsvConverter;
import criterias.LogEntryCriteria;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;

/**
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
@With(ContextAction.class)
public class Users extends AbstractController {
  @Inject
  public Users(Injector injector, CacheApi cache, PlayAuthenticate auth) {
    super(injector, cache, auth);
  }

  public Result user(UUID id) {
    return user(id, user -> {
      if (user.id.equals(User.loggedInUserId()))
        return redirect(routes.Profiles.profile());

      return ok(
          views.html.users.user.render(createTemplate(), user, userService.getUserStats(user.id)));
    });
  }

  public Result activityCsv(UUID userId) {
    return user(userId, user -> {
      return ok(new ActivityCsvConverter()
          .apply(logEntryService.getAggregates(new LogEntryCriteria().withUserId(user.id))));
    });
  }

  /**
   * @param userId
   * @param object
   * @return
   */
  private Result user(UUID userId, Function<User, Result> processor) {
    User user = userService.byId(userId);
    if (user == null)
      return redirectWithError(routes.Application.index(), "user.notFound");

    return processor.apply(user);
  }
}