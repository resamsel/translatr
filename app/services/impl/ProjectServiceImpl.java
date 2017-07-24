package services.impl;

import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

import com.avaje.ebean.PagedList;
import criterias.ProjectCriteria;
import dto.NotFoundException;
import dto.PermissionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.ActionType;
import models.Locale;
import models.LogEntry;
import models.Message;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
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

  private final CacheApi cache;

  private final LocaleService localeService;

  private final KeyService keyService;

  private final MessageService messageService;

  @Inject
  public ProjectServiceImpl(Validator validator, CacheApi cache, LocaleService localeService,
      KeyService keyService, MessageService messageService, LogEntryService logEntryService) {
    super(validator, logEntryService);
    this.cache = cache;
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<Project> findBy(ProjectCriteria criteria) {
    return Project.findBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Project byId(UUID id, String... fetches) {
    return log(() -> cache.getOrElse(Project.getCacheKey(id, fetches),
        () -> Project.byId(id, fetches), 60), LOGGER, "byId(fetches={})", Arrays.asList(fetches));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Project byOwnerAndName(String username, String name, String... fetches) {
    return log(() -> cache.getOrElse(Project.getCacheKey(username, name, fetches),
        () -> Project.byOwnerAndName(username, name, fetches), 10 * 600), LOGGER, "byOwnerAndName");
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

    Project project = Project.byId(projectId);

    if (project == null) {
      return;
    }

    if (project.wordCount == null) {
      project.wordCount = 0;
    }
    project.wordCount += wordCountDiff;

    log(() -> persist(project), LOGGER, "Increased word count by %d", wordCountDiff);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    Project project = byId(projectId, Project.FETCH_LOCALES);
    List<UUID> localeIds = project.locales.stream().map(Locale::getId).collect(toList());

    project.wordCount = null;

    persist(project);

    localeService.resetWordCount(projectId);
    keyService.resetWordCount(projectId);
    messageService.resetWordCount(projectId);
    messageService.save(Message.byLocales(localeIds));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(Project t, boolean update) {
    if (t.owner == null) {
      t.owner = User.loggedInUser();
    }
    if (t.members == null) {
      t.members = new ArrayList<>();
    }
    if (t.members.isEmpty()) {
      t.members.add(new ProjectUser(ProjectRole.Owner).withProject(t).withUser(t.owner));
    }
  }

  @Override
  protected void prePersist(Project t, boolean update) {
    if (update) {
      logEntryService.save(
          LogEntry.from(ActionType.Update, t, dto.Project.class, toDto(byId(t.id)), toDto(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Project t, boolean update) {
    if (!update) {
      logEntryService.save(LogEntry.from(ActionType.Create, t, dto.Project.class, null, toDto(t)));
    }

    // When message has been created, the project cache needs to be invalidated
    cache.remove(Project.getCacheKey(t.id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Project t) {
    if (t == null || t.deleted) {
      throw new NotFoundException(dto.Project.class.getSimpleName(), t != null ? t.id : null);
    }
    if (!t.hasPermissionAny(User.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager)) {
      throw new PermissionException("User not allowed in project");
    }

    keyService.delete(t.keys);
    localeService.delete(t.locales);

    logEntryService.save(LogEntry.from(ActionType.Delete, t, dto.Project.class, toDto(t), null));

    super.save(t.withName(String.format("%s-%s", t.id, t.name)).withDeleted(true));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Collection<Project> t) {
    User loggedInUser = User.loggedInUser();
    for (Project p : t) {
      if (p == null || p.deleted) {
        throw new NotFoundException(dto.Project.class.getSimpleName(), p != null ? p.id : null);
      }
      if (!p.hasPermissionAny(loggedInUser, ProjectRole.Owner, ProjectRole.Manager)) {
        throw new PermissionException("User not allowed in project");
      }
    }

    keyService
        .delete(t.stream().map(p -> p.keys).flatMap(k -> k.stream()).collect(Collectors.toList()));
    localeService.delete(
        t.stream().map(p -> p.locales).flatMap(l -> l.stream()).collect(Collectors.toList()));

    logEntryService.save(
        t.stream().map(p -> LogEntry.from(ActionType.Delete, p, dto.Project.class, toDto(p), null))
            .collect(Collectors.toList()));

    super.save(
        t.stream().map(p -> p.withName(String.format("%s-%s", p.id, p.name)).withDeleted(true))
            .collect(Collectors.toList()));
  }

  protected dto.Project toDto(Project t) {
    dto.Project out = dto.Project.from(t);

    out.keys = Collections.emptyList();
    out.locales = Collections.emptyList();
    out.messages = Collections.emptyList();

    return out;
  }
}
