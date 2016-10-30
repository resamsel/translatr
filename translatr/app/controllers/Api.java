package controllers;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import dto.NotFoundException;
import dto.PermissionException;
import models.Locale;
import models.Message;
import models.Project;
import models.ProjectRole;
import models.Scope;
import models.User;
import play.cache.CacheApi;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.With;
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;
import services.ProjectService;
import services.UserService;
import utils.PermissionUtils;

@With(ApiAction.class)
public class Api extends AbstractController {
  private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

  private final ProjectService projectService;

  private final LocaleService localeService;

  private final MessageService messageService;

  /**
   * 
   */
  @Inject
  public Api(Injector injector, CacheApi cache, PlayAuthenticate auth, UserService userService,
      LogEntryService logEntryService, ProjectService projectService, LocaleService localeService,
      MessageService messageService) {
    super(injector, cache, auth, userService, logEntryService);

    this.projectService = projectService;
    this.localeService = localeService;
    this.messageService = messageService;
  }

  /**
   * @param errorMessage
   * @param scopes
   */
  private void checkPermissionAll(String errorMessage, Scope... scopes) {
    if (!PermissionUtils.hasPermissionAll(scopes))
      throw new PermissionException(errorMessage);
  }

  private void checkProjectRole(Project project, User user, ProjectRole role) {
    if (!project.hasPermission(user, role))
      throw new PermissionException("User not allowed in project");
  }

  private Result catchError(Supplier<Result> supplier) {
    try {
      return supplier.get();
    } catch (PermissionException e) {
      return forbidden(e.toJson());
    } catch (NotFoundException e) {
      return notFound(e.toJson());
    } catch (Exception e) {
      return badRequest(Json.toJson(e));
    }
  }

  private Result project(UUID projectId, Function<Project, Result> processor) {
    return catchError(() -> {
      Project project = Project.byId(projectId);
      if (project == null)
        throw new NotFoundException(String.format("Project not found: %s", projectId));

      return processor.apply(project);
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result createProject() {
    return catchError(() -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectWrite);

      JsonNode json = request().body().asJson();

      Project project = null;
      if (json.has("id")) {
        project = Project.byId(UUID.fromString(json.get("id").asText()));
      } else {
        project = Json.fromJson(json, Project.class);
        LOGGER.debug("Project: {}", Json.toJson(project));
        projectService.save(project);
      }

      return ok(Json.toJson(dto.Project.from(project)));
    });
  }

  public Result findLocales(UUID projectId) {
    return project(projectId, project -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead);
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Translator);

      List<Locale> locales = Locale.findBy(new LocaleCriteria().withProjectId(project.id)
          .withLocaleName(request().getQueryString("localeName")));

      return ok(
          Json.toJson(locales.stream().map(l -> dto.Locale.from(l)).collect(Collectors.toList())));
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result createLocale() {
    return catchError(() -> {
      JsonNode json = request().body().asJson();

      Locale locale = null;
      if (json.has("id")) {
        locale = Locale.byId(UUID.fromString(json.get("id").asText()));
      } else {
        locale = Json.fromJson(json, Locale.class);
        LOGGER.debug("Locale: {}", Json.toJson(locale));

        checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleWrite);
        checkProjectRole(locale.project, User.loggedInUser(), ProjectRole.Translator);

        localeService.save(locale);
      }

      return ok(Json.toJson(dto.Locale.from(locale)));
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result createMessage() {
    return ok(Json.toJson(dto.Message.from(messageService.create(request().body().asJson()))));
  }

  public Result findMessages(UUID projectId) {
    List<Message> messages = Message.findBy(new MessageCriteria().withProjectId(projectId)
        .withKeyName(request().getQueryString("keyName")));

    return ok(
        Json.toJson(messages.stream().map(m -> dto.Message.from(m)).collect(Collectors.toList())));
  }

  public Result getMessage(UUID localeId, String key) {
    return catchError(() -> {
      Message message = Message.byLocaleAndKeyName(localeId, key);

      if (message == null)
        throw new PermissionException("Message not found");

      return ok(Json.toJson(dto.Message.from(message)));
    });
  }

  public Result uploadLocale(UUID localeId, String fileType) {
    return catchError(() -> locale(localeId, locale -> {
      injector.instanceOf(Locales.class).importLocale(locale, request());

      return ok("{\"status\":\"OK\"}");
    }));
  }

  public Result downloadLocale(UUID localeId, String fileType) {
    return injector.instanceOf(Locales.class).download(localeId, fileType);
  }
}
