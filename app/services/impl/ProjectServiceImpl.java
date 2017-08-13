package services.impl;

import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

import criterias.ProjectCriteria;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.Locale;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import repositories.ProjectRepository;
import services.CacheService;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;
import services.ProjectService;

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

  @Inject
  public ProjectServiceImpl(Validator validator, CacheService cache,
      ProjectRepository projectRepository, LocaleService localeService, KeyService keyService,
      MessageService messageService, MessageRepository messageRepository,
      LogEntryService logEntryService) {
    super(validator, cache, projectRepository, Project::getCacheKey, logEntryService);

    this.projectRepository = projectRepository;
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
    this.messageRepository = messageRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Project byOwnerAndName(String username, String name, String... fetches) {
    return log(
        () -> cache.getOrElse(
            Project.getCacheKey(username, name, fetches),
            () -> projectRepository.byOwnerAndName(username, name, fetches),
            10 * 600
        ),
        LOGGER,
        "byOwnerAndName"
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void increaseWordCountBy(UUID projectId, int wordCountDiff) {
    if (wordCountDiff == 0) {
      LOGGER.debug("Not changing word count");
      return;
    }

    Project project = byId(projectId);
    if (project == null) {
      return;
    }

    if (project.wordCount == null) {
      project.wordCount = 0;
    }
    project.wordCount += wordCountDiff;

    log(
        () -> modelRepository.persist(project),
        LOGGER,
        "Increased word count by %d",
        wordCountDiff
    );

    postUpdate(project);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    Project project = byId(projectId, ProjectRepository.FETCH_LOCALES);
    List<UUID> localeIds = project.locales.stream().map(Locale::getId).collect(toList());

    project.wordCount = null;

    modelRepository.persist(project);

    postUpdate(project);

    localeService.resetWordCount(projectId);
    keyService.resetWordCount(projectId);
    messageService.resetWordCount(projectId);
    messageService.save(messageRepository.byLocales(localeIds));
  }

  @Override
  protected void postSave(Project t) {
    super.postSave(t);

    // When project has been created, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:");
  }

  @Override
  protected void postUpdate(Project t) {
    // When project has been updated, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:");

    Project existing = cache.get(Project.getCacheKey(t.id));
    if (existing != null) {
      cache.removeByPrefix(Project.getCacheKey(existing.owner.username, existing.name));
    }

    super.postUpdate(t);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Project t) {
    super.postDelete(t);

    // When key has been deleted, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:" + t.id);

    cache.removeByPrefix(Project.getCacheKey(t.owner.username, t.name));
  }
}
