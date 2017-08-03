package repositories.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static models.Project.FETCH_MEMBERS;
import static models.Project.FETCH_OWNER;
import static utils.Stopwatch.log;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.google.common.collect.ImmutableMap;
import criterias.HasNextPagedList;
import criterias.ProjectCriteria;
import dto.NotFoundException;
import dto.PermissionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.ActionType;
import models.LogEntry;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import repositories.KeyRepository;
import repositories.LocaleRepository;
import repositories.LogEntryRepository;
import repositories.ProjectRepository;
import services.PermissionService;
import utils.QueryUtils;

@Singleton
public class ProjectRepositoryImpl extends
    AbstractModelRepository<Project, UUID, ProjectCriteria> implements
    ProjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepositoryImpl.class);

  private static final String[] PROPERTIES_TO_FETCH = {FETCH_OWNER, FETCH_MEMBERS};

  private static final Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of("project", singletonList("project"), FETCH_MEMBERS,
          asList(FETCH_MEMBERS, FETCH_MEMBERS + ".user"));

  private final LocaleRepository localeRepository;
  private final KeyRepository keyRepository;
  private final PermissionService permissionService;

  private final Find<UUID, Project> find = new Find<UUID, Project>() {
  };

  @Inject
  public ProjectRepositoryImpl(Validator validator, CacheApi cache,
      LocaleRepository localeRepository, KeyRepository keyRepository,
      LogEntryRepository logEntryRepository, PermissionService permissionService) {
    super(validator, cache, logEntryRepository);

    this.localeRepository = localeRepository;
    this.keyRepository = keyRepository;
    this.permissionService = permissionService;
  }

  @Override
  public PagedList<Project> findBy(ProjectCriteria criteria) {
    ExpressionList<Project> query = find.fetch(FETCH_OWNER).fetch(FETCH_MEMBERS)
        .fetch(Project.FETCH_LOCALES).fetch(Project.FETCH_KEYS).where();

    query.eq("deleted", false);

    if (criteria.getOwnerId() != null) {
      query.eq("owner.id", criteria.getOwnerId());
    }

    if (criteria.getMemberId() != null) {
      query.eq("members.user.id", criteria.getMemberId());
    }

    if (criteria.getProjectId() != null) {
      query.idEq(criteria.getProjectId());
    }

    if (criteria.getSearch() != null) {
      query.ilike("name", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return log(() -> HasNextPagedList.create(query), LOGGER, "findBy");
  }

  @Override
  public Project byId(UUID id, String... fetches) {
    if (id == null) {
      return null;
    }

    return QueryUtils
        .fetch(find.setId(id), QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP)
        .findUnique();
  }

  @Override
  public Project byOwnerAndName(String username, String name, String... fetches) {
    return QueryUtils
        .fetch(find.query(), QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP)
        .where()
        .eq("owner.username", username)
        .eq("name", name)
        .findUnique();
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
      logEntryRepository.save(
          LogEntry.from(ActionType.Update, t, dto.Project.class, toDto(byId(t.id)), toDto(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Project t, boolean update) {
    if (!update) {
      logEntryRepository
          .save(LogEntry.from(ActionType.Create, t, dto.Project.class, null, toDto(t)));
    }

    // When message has been created, the project cache needs to be invalidated
    cache.remove(Project.getCacheKey(t.id, PROPERTIES_TO_FETCH));
    cache.remove(Project.getCacheKey(t.owner.username, t.name));
    cache.remove(new ProjectCriteria().withMemberId(User.loggedInUserId()).getCacheKey());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Project t) {
    if (t == null || t.deleted) {
      throw new NotFoundException(dto.Project.class.getSimpleName(), t != null ? t.id : null);
    }
    if (!permissionService
        .hasPermissionAny(t.id, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager)) {
      throw new PermissionException("User not allowed in project");
    }

    localeRepository.delete(t.locales);
    keyRepository.delete(t.keys);

    logEntryRepository.save(LogEntry.from(ActionType.Delete, t, dto.Project.class, toDto(t), null));

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
      if (!permissionService
          .hasPermissionAny(p.id, loggedInUser, ProjectRole.Owner, ProjectRole.Manager)) {
        throw new PermissionException("User not allowed in project");
      }
    }

    keyRepository
        .delete(
            t.stream().map(p -> p.keys).flatMap(Collection::stream).collect(Collectors.toList()));
    localeRepository.delete(
        t.stream().map(p -> p.locales).flatMap(Collection::stream).collect(Collectors.toList()));

    logEntryRepository.save(
        t.stream().map(p -> LogEntry.from(ActionType.Delete, p, dto.Project.class, toDto(p), null))
            .collect(Collectors.toList()));

    super.save(
        t.stream().map(p -> p.withName(String.format("%s-%s", p.id, p.name)).withDeleted(true))
            .collect(Collectors.toList()));
  }

  private dto.Project toDto(Project t) {
    dto.Project out = dto.Project.from(t);

    out.keys = Collections.emptyList();
    out.locales = Collections.emptyList();
    out.messages = Collections.emptyList();

    return out;
  }
}
