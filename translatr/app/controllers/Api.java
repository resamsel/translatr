package controllers;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ApiAction;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import criterias.ProjectCriteria;
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
import utils.ErrorUtils;
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

  private void checkProjectRole(Project project, User user, ProjectRole... roles) {
    if (!project.hasPermissionAny(user, roles))
      throw new PermissionException("User not allowed in project");
  }

  @Override
  protected Result catchError(Supplier<Result> supplier) {
    try {
      return supplier.get();
    } catch (PermissionException e) {
      return forbidden(e.toJson());
    } catch (NotFoundException e) {
      return notFound(e.toJson());
    } catch (ValidationException e) {
      return badRequest(ErrorUtils.toJson(e));
    } catch (Exception e) {
      return badRequest(ErrorUtils.toJson(e));
    }
  }

  private Result project(UUID projectId, Function<Project, Result> processor) {
    return catchError(() -> {
      Project project = Project.byId(projectId);
      if (project == null)
        throw new NotFoundException(String.format("Project not found: '%s'", projectId));

      return processor.apply(project);
    });
  }

  public Result findProjects() {
    return loggedInUser(user -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead);

      List<Project> projects = Project.findBy(new ProjectCriteria().withMemberId(user.id));

      return ok(Json
          .toJson(projects.stream().map(p -> dto.Project.from(p)).collect(Collectors.toList())));
    });
  }

  public Result getProject(UUID projectId) {
    return project(projectId, project -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead);

      return ok(Json.toJson(dto.Project.from(project)));
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result createProject() {
    return loggedInUser(user -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectWrite);

      dto.Project json = Json.fromJson(request().body().asJson(), dto.Project.class);

      if (json.name != null)
        if (Project.byOwnerAndName(user, json.name) != null)
          throw new IllegalArgumentException(
              String.format("Project with name '%s' already exists", json.name));

      Project project = json.toModel();
      project.owner = user;

      LOGGER.debug("Project: {}", Json.toJson(project));
      projectService.save(project);

      return ok(Json.toJson(dto.Project.from(project)));
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result updateProject() {
    return loggedInUser(user -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectWrite);

      dto.Project json = Json.fromJson(request().body().asJson(), dto.Project.class);
      if (json.id == null)
        throw new IllegalArgumentException("Field 'id' required");

      return project(json.id, project -> {
        checkProjectRole(project, user, ProjectRole.Owner);

        project.updateFrom(json.toModel());
        project.owner = user;

        LOGGER.debug("Project: {}", Json.toJson(project));
        projectService.save(project);

        return ok(Json.toJson(dto.Project.from(project)));
      });
    });
  }

  public Result deleteProject(UUID projectId) {
    return project(projectId, project -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectWrite);
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner);

      projectService.delete(project);

      return ok(Json.newObject().put("message",
          String.format("Project with ID '%s' has been deleted", projectId)));
    });
  }

  public Result findLocales(UUID projectId) {
    return project(projectId, project -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead);
      checkProjectRole(project, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Translator);

      List<Locale> locales = Locale.findBy(new LocaleCriteria().withProjectId(project.id)
          .withLocaleName(request().getQueryString("localeName")));

      return ok(
          Json.toJson(locales.stream().map(l -> dto.Locale.from(l)).collect(Collectors.toList())));
    });
  }

  @BodyParser.Of(BodyParser.Json.class)
  public Result createLocale() {
    return catchError(() -> {
      checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleWrite);

      dto.Locale json = Json.fromJson(request().body().asJson(), dto.Locale.class);

      if (json.projectId == null)
        throw new ValidationException("Field 'projectId' required");

      return project(json.projectId, project -> {
        checkProjectRole(project, User.loggedInUser(), ProjectRole.Translator);

        if (json.name == null)
          throw new ValidationException("Field 'name' required");
        else if (Locale.byProjectAndName(project, json.name) != null)
          throw new ValidationException(String.format(
              "Locale with name '%s' already exists in project '%s'", json.name, project.name));

        Locale locale = json.toModel(project);

        LOGGER.debug("Project: {}", Json.toJson(project));
        localeService.save(locale);

        return ok(Json.toJson(dto.Locale.from(locale)));
      });
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
