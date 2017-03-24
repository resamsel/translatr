package services.impl;

import static utils.Stopwatch.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.PagedList;

import criterias.ProjectCriteria;
import dto.NotFoundException;
import dto.PermissionException;
import models.ActionType;
import models.LogEntry;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.ProjectService;

/**
 *
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

  /**
   * 
   */
  @Inject
  public ProjectServiceImpl(Configuration configuration, Validator validator, CacheApi cache,
      LocaleService localeService, KeyService keyService, LogEntryService logEntryService) {
    super(configuration, validator, logEntryService);
    this.cache = cache;
    this.localeService = localeService;
    this.keyService = keyService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<Project> findBy(ProjectCriteria criteria) {
    return Project.pagedBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Project byId(UUID id, String... fetches) {
    return log(() -> cache.getOrElse(Project.getCacheKey(id), () -> Project.byId(id), 60), LOGGER,
        "byId");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Project byOwnerAndName(User user, String name) {
    return log(() -> cache.getOrElse(
        String.format("projectByOwnerAndName:%s:%s", user.id.toString(), name),
        () -> Project.byOwnerAndName(user, name), 10 * 600), LOGGER, "byOwnerAndName");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(Project t, boolean update) {
    if (t.owner == null)
      t.owner = User.loggedInUser();
    if (t.members == null)
      t.members = new ArrayList<>();
    if (t.members.isEmpty())
      t.members.add(new ProjectUser(ProjectRole.Owner).withProject(t).withUser(t.owner));

    if (update)
      logEntryService.save(
          LogEntry.from(ActionType.Update, t, dto.Project.class, toDto(byId(t.id)), toDto(t)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Project t, boolean update) {
    if (!update)
      logEntryService.save(LogEntry.from(ActionType.Create, t, dto.Project.class, null, toDto(t)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Project t) {
    if (t == null || t.deleted)
      throw new NotFoundException(dto.Project.class.getSimpleName(), t != null ? t.id : null);
    if (!t.hasPermissionAny(User.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager))
      throw new PermissionException("User not allowed in project");

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
      if (p == null || p.deleted)
        throw new NotFoundException(dto.Project.class.getSimpleName(), p != null ? p.id : null);
      if (!p.hasPermissionAny(loggedInUser, ProjectRole.Owner, ProjectRole.Manager))
        throw new PermissionException("User not allowed in project");
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
