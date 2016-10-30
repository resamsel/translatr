package controllers;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;
import services.MessageService;
import services.UserService;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
@With(ContextAction.class)
@SubjectPresent(forceBeforeAuthCheck = true)
public class Translations extends AbstractController {
  private final MessageService messageService;

  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   */
  @Inject
  protected Translations(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, MessageService messageService) {
    super(injector, cache, auth, userService, logEntryService);

    this.messageService = messageService;
  }

  public Result create() {
    return ok(Json.toJson(dto.Message.from(messageService.create(request().body().asJson()))));
  }
}
