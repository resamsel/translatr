package controllers;

import java.util.UUID;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.MessageCriteria;
import models.Message;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.LogEntryService;
import services.MessageService;
import services.UserService;
import utils.JsonUtils;

/**
 * @author resamsel
 * @version 10 Jan 2017
 */
@With(ApiAction.class)
public class TranslationsApi extends Api<Message, dto.Message, UUID> {
  /**
   * @param injector
   * @param cache
   * @param auth
   * @param userService
   * @param logEntryService
   */
  @Inject
  public TranslationsApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      UserService userService, LogEntryService logEntryService, MessageService messageService) {
    super(injector, cache, auth, userService, logEntryService, messageService, Message::byId,
        dto.Message.class, dto.Message::from, Message::from, new Scope[] {Scope.ProjectRead},
        new Scope[] {Scope.ProjectRead});
  }

  public Result find(UUID projectId) {
    return project(projectId, project -> {
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Translator,
          ProjectRole.Developer);

      return toJsons(dto.Message::from,
          finder(Message::findBy,
              new MessageCriteria().withProjectId(project.id)
                  .withLocaleId(JsonUtils.getUuid(request().getQueryString("localeId")))
                  .withKeyName(request().getQueryString("keyName")),
              readScopes));
    });
  }
}
