package controllers;

import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.MessageCriteria;
import dto.NotFoundException;
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
    super(injector, cache, auth, userService, logEntryService, messageService);
  }

  public Result find(UUID projectId) {
    return project(projectId, project -> {
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Translator,
          ProjectRole.Developer);

      return toJsons(dto.Message::from, finder(new MessageCriteria().withProjectId(project.id)
          .withKeyName(request().getQueryString("keyName")), Message::findBy, Scope.ProjectRead));
    });
  }

  public Result getByLocaleAndKey(UUID localeId, String key) {
    return toJson(dto.Message::from, () -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead);

      Message message = Message.byLocaleAndKeyName(localeId, key);

      if (message == null)
        throw new NotFoundException("Message not found");

      return message;
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Function<Message, dto.Message> dtoMapper() {
    return dto.Message::from;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Function<UUID, Message> getter() {
    return Message::byId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesGet() {
    return new Scope[] {Scope.ProjectRead};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesCreate() {
    return new Scope[] {Scope.ProjectRead};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Scope[] scopesDelete() {
    return new Scope[] {Scope.ProjectRead};
  }
}
