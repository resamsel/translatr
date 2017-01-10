package controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import criterias.MessageCriteria;
import dto.NotFoundException;
import models.Message;
import models.Project;
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

  public Result update() {
    return ok(Json.toJson(dto.Message.from(messageService.update(request().body().asJson()))));
  }

  public Result getByLocaleAndKey(UUID localeId, String keyName) {
    Message message = Message.byLocaleAndKeyName(localeId, keyName);

    if (message == null)
      return notFound(Json.newObject().put("message", "Message not found"));

    return ok(Json.toJson(dto.Message.from(message)));
  }

  public Result find(UUID projectId) {
    Project project = Project.byId(projectId);
    if (project == null || project.deleted)
      throw new NotFoundException(String.format("Project not found: '%s'", projectId));

    List<Message> messages = Message.findBy(new MessageCriteria().withProjectId(project.id)
        .withKeyName(request().getQueryString("keyName")));

    return ok(Json.toJson(messages.stream().map(dto.Message::from).collect(Collectors.toList())));
  }
}
