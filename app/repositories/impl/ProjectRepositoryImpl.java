package repositories.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol.Activities;
import actors.ActivityProtocol.Activity;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.RawSqlBuilder;
import criterias.PagedListFactory;
import criterias.ProjectCriteria;
import dto.NotFoundException;
import dto.PermissionException;
import mappers.ProjectMapper;
import models.ActionType;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Stat;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.KeyRepository;
import repositories.LocaleRepository;
import repositories.Persistence;
import repositories.ProjectRepository;
import services.AuthProvider;
import services.PermissionService;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

@Singleton
public class ProjectRepositoryImpl extends
    AbstractModelRepository<Project, UUID, ProjectCriteria> implements
    ProjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepositoryImpl.class);
  private static final String PROGRESS_COLUMN_ID = "p.id";
  private static final String PROGRESS_COLUMN_COUNT = "cast(count(distinct m.id) as decimal)/cast(count(distinct l.id)*count(distinct k.id) as decimal)";

  private final LocaleRepository localeRepository;
  private final KeyRepository keyRepository;
  private final PermissionService permissionService;

  private final Find<UUID, Project> find = new Find<UUID, Project>() {
  };

  @Inject
  public ProjectRepositoryImpl(Persistence persistence,
                               Validator validator,
                               AuthProvider authProvider,
                               ActivityActorRef activityActor,
                               LocaleRepository localeRepository,
                               KeyRepository keyRepository,
                               PermissionService permissionService) {
    super(persistence, validator, authProvider, activityActor);

    this.localeRepository = localeRepository;
    this.keyRepository = keyRepository;
    this.permissionService = permissionService;
  }

  @Override
  public PagedList<Project> findBy(ProjectCriteria criteria) {
    ExpressionList<Project> query = QueryUtils.fetch(
        find.query().setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, criteria.getFetches()),
        FETCH_MAP
    ).where();

    query.eq("deleted", false);

    if (criteria.getOwnerId() != null) {
      query.eq("owner.id", criteria.getOwnerId());
    }

    if (criteria.getOwnerUsername() != null) {
      query.eq("owner.username", criteria.getOwnerUsername());
    }

    if (criteria.getMemberId() != null) {
      query.eq("members.user.id", criteria.getMemberId());
    }

    if (criteria.getName() != null) {
      query.eq("name", criteria.getName());
    }

    if (criteria.getProjectId() != null) {
      query.idEq(criteria.getProjectId());
    }

    if (criteria.getSearch() != null) {
      query.disjunction()
          .ilike("name", "%" + criteria.getSearch() + "%")
          .ilike("description", "%" + criteria.getSearch() + "%")
          .ilike("owner.name", "%" + criteria.getSearch() + "%")
          .ilike("owner.username", "%" + criteria.getSearch() + "%")
          .endJunction();
    }

    if (criteria.getOrder() != null) {
      query.setOrderBy(criteria.getOrder());
    }

    criteria.paged(query);

    return fetch(
        log(() -> PagedListFactory.create(query, criteria.hasFetch(FETCH_COUNT)), LOGGER, "findBy"),
        criteria
    );
  }

  private PagedList<Project> fetch(PagedList<Project> paged, ProjectCriteria criteria) {
    if (criteria.hasFetch(FETCH_PROGRESS)) {
      Map<UUID, Double> progressMap = progress(paged.getList().stream().map(Project::getId).collect(toList()));

      paged.getList()
          .forEach(p -> p.progress = progressMap.getOrDefault(p.id, 0.0));
    }

    return paged;
  }

  @Override
  public Map<UUID, Double> progress(List<UUID> projectIds) {
    List<Stat> stats = log(
        () -> persistence.createQuery(Stat.class)
            .setRawSql(RawSqlBuilder
                .parse("SELECT " +
                    PROGRESS_COLUMN_ID + ", " + PROGRESS_COLUMN_COUNT +
                    " FROM project p" +
                    " JOIN locale l ON l.project_id = p.id" +
                    " JOIN key k ON k.project_id = p.id" +
                    " LEFT OUTER JOIN message m ON m.key_id = k.id" +
                    " GROUP BY " + PROGRESS_COLUMN_ID)
                .columnMapping(PROGRESS_COLUMN_ID, "id")
                .columnMapping(PROGRESS_COLUMN_COUNT, "count")
                .create())
            .where()
            .in("p.id", projectIds)
            .findList(),
        LOGGER,
        "Retrieving project progress"
    );

    return stats.stream().collect(Collectors.toMap(stat -> stat.id, stat -> stat.count));
  }

  @Override
  public Project byId(UUID id, String... fetches) {
    if (id == null) {
      return null;
    }

    return QueryUtils
        .fetch(
            find.query().setDisableLazyLoading(true),
            QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches),
            FETCH_MAP
        )
        .setId(id)
        .findUnique();
  }

  @Override
  public Project byOwnerAndName(String username, String name, String... fetches) {
    return QueryUtils
        .fetch(
            find.query().setDisableLazyLoading(true),
            QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches),
            FETCH_MAP
        )
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
    persistence.markAsDirty(t);
    if (t.owner == null || t.owner.id == null) {
      t.owner = authProvider.loggedInUser();
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
      activityActor.tell(
          new Activity<>(ActionType.Update, authProvider.loggedInUser(), t, dto.Project.class, toDto(byId(t.id)), toDto(t)),
          null
      );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Project t, boolean update) {
    super.postSave(t, update);
    persistence.refresh(t);
    persistence.refresh(t.owner);

    if (!update) {
      activityActor.tell(
          new Activity<>(ActionType.Create, authProvider.loggedInUser(), t, dto.Project.class, null, toDto(t)),
          null
      );
    }
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
        .hasPermissionAny(t.id, authProvider.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager)) {
      throw new PermissionException("User not allowed in project");
    }

    localeRepository.delete(t.locales);
    keyRepository.delete(t.keys);

    activityActor.tell(
        new Activity<>(ActionType.Delete, authProvider.loggedInUser(), t, dto.Project.class, toDto(t), null),
        null
    );

    super.save(t.withName(String.format("%s-%s", t.id, t.name)).withDeleted(true));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Collection<Project> t) {
    User loggedInUser = authProvider.loggedInUser();
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
            t.stream().map(p -> p.keys).flatMap(Collection::stream).collect(toList()));
    localeRepository.delete(
        t.stream().map(p -> p.locales).flatMap(Collection::stream).collect(toList()));

    activityActor.tell(
        new Activities<>(t.stream()
            .map(p -> new Activity<>(ActionType.Delete, authProvider.loggedInUser(), p, dto.Project.class, toDto(p), null))
            .collect(toList())),
        null
    );

    super.save(
        t.stream().map(p -> p.withName(String.format("%s-%s", p.id, p.name)).withDeleted(true))
            .collect(toList()));
  }

  private dto.Project toDto(Project t) {
    dto.Project out = ProjectMapper.toDto(t);

    out.keys = Collections.emptyList();
    out.locales = Collections.emptyList();
    out.messages = Collections.emptyList();

    return out;
  }
}
