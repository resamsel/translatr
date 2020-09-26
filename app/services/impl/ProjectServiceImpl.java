package services.impl;

import criterias.GetCriteria;
import io.ebean.PagedList;
import criterias.ProjectCriteria;
import models.ActionType;
import models.Locale;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.MessageRepository;
import repositories.ProjectRepository;
import services.AuthProvider;
import services.CacheService;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;
import services.MetricService;
import services.ProjectService;
import services.ProjectUserService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class ProjectServiceImpl extends AbstractModelService<Project, UUID, ProjectCriteria>
    implements ProjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

  private final ProjectRepository projectRepository;
  private final LocaleService localeService;
  private final KeyService keyService;
  private final MessageService messageService;
  private final MessageRepository messageRepository;
  private final ProjectUserService projectUserService;
  private final MetricService metricService;

  @Inject
  public ProjectServiceImpl(Validator validator, CacheService cache,
                            ProjectRepository projectRepository, LocaleService localeService, KeyService keyService,
                            MessageService messageService, MessageRepository messageRepository,
                            ProjectUserService projectUserService, LogEntryService logEntryService,
                            AuthProvider authProvider, MetricService metricService) {
    super(validator, cache, projectRepository, Project::getCacheKey, logEntryService, authProvider);

    this.projectRepository = projectRepository;
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
    this.messageRepository = messageRepository;
    this.projectUserService = projectUserService;
    this.metricService = metricService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Project byOwnerAndName(String username, String name, Http.Request request, String... fetches) {
    return log(
            () -> postGet(cache.getOrElseUpdate(
                    Project.getCacheKey(username, name, fetches),
                    () -> projectRepository.byOwnerAndName(username, name, fetches),
                    10 * 30
            ), request),
            LOGGER,
            "byOwnerAndName"
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void increaseWordCountBy(UUID projectId, int wordCountDiff, Http.Request request) {
    if (wordCountDiff == 0) {
      LOGGER.debug("Not changing word count");
      return;
    }

    Project project = byId(GetCriteria.from(projectId, request));
    if (project == null) {
      return;
    }

    if (project.wordCount == null) {
      project.wordCount = 0;
    }
    project.wordCount += wordCountDiff;

    log(
        () -> modelRepository.save(project),
        LOGGER,
        "Increased word count by %d",
        wordCountDiff
    );

    postUpdate(project, request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId, Http.Request request) {
    Project project = byId(projectId, request, ProjectRepository.FETCH_LOCALES);
    List<UUID> localeIds = project.locales.stream().map(Locale::getId).collect(toList());

    project.wordCount = null;

    modelRepository.save(project);

    postUpdate(project, request);

    localeService.resetWordCount(projectId);
    keyService.resetWordCount(projectId);
    messageService.resetWordCount(projectId);
    messageService.save(messageRepository.byLocales(localeIds), request);
  }

  @Override
  public void changeOwner(Project project, User owner, Http.Request request) {
    LOGGER.debug("changeOwner(project={}, owner={})", project, owner);

    requireNonNull(project, "project");
    requireNonNull(owner, "owner");

    // Make old owner a member of type Manager
    ProjectUser ownerRole = project.members.stream()
        .filter(m -> m.role == ProjectRole.Owner)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Project has no owner"));

    ownerRole.role = ProjectRole.Manager;

    // Make new owner a member of type Owner
    ProjectUser newOwnerRole = project.members.stream()
        .filter(m -> m.user.id.equals(owner.id))
        .findFirst()
        .orElseGet(() -> new ProjectUser(ProjectRole.Manager).withProject(project).withUser(owner));

    newOwnerRole.role = ProjectRole.Owner;

    if (newOwnerRole.id == null) {
      project.members.add(newOwnerRole);
    }

    update(project.withOwner(owner), request);
    projectUserService.update(ownerRole, request);
    projectUserService.update(newOwnerRole, request);
  }

  @Override
  protected PagedList<Project> postFind(PagedList<Project> pagedList, Http.Request request) {
    metricService.logEvent(Project.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  @Override
  protected Project postGet(Project project, Http.Request request) {
    metricService.logEvent(Project.class, ActionType.Read);

    return super.postGet(project, request);
  }

  @Override
  protected void preCreate(Project t, Http.Request request) {
    if (t.owner == null || t.owner.id == null) {
      t.owner = authProvider.loggedInUser(request);
    }

    super.preCreate(t, request);
  }

  @Override
  protected void postCreate(Project t, Http.Request request) {
    super.postCreate(t, request);

    projectUserService.create(new ProjectUser().withProject(t).withUser(t.owner).withRole(ProjectRole.Owner), request);

    metricService.logEvent(Project.class, ActionType.Create);

    // When project has been created, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:");
  }

  @Override
  protected Project postUpdate(Project t, Http.Request request) {
    metricService.logEvent(Project.class, ActionType.Update);

    // When project has been updated, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:");

    Optional<Project> cached = cache.get(Project.getCacheKey(t.id));
    if (cached.isPresent()) {
      cache.removeByPrefix(Project.getCacheKey(
              requireNonNull(cached.get().owner, "owner (cached)").username,
              cached.get().name
      ));
    } else {
      cache.removeByPrefix(Project.getCacheKey(
          requireNonNull(t.owner, "owner").username,
          ""
      ));
    }

    return super.postUpdate(t, request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Project t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(Project.class, ActionType.Delete);

    // When key has been deleted, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:" + t.id);

    cache.removeByPrefix(Project.getCacheKey(t.owner.username, t.name));
  }
}
