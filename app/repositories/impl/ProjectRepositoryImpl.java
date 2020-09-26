package repositories.impl;

import criterias.ContextCriteria;
import criterias.DefaultContextCriteria;
import criterias.GetCriteria;
import criterias.PagedListFactory;
import criterias.ProjectCriteria;
import dto.NotFoundException;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import io.ebean.RawSqlBuilder;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.KeyRepository;
import repositories.LocaleRepository;
import repositories.Persistence;
import repositories.ProjectRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.Stopwatch.log;

@Singleton
public class ProjectRepositoryImpl extends
        AbstractModelRepository<Project, UUID, ProjectCriteria> implements
        ProjectRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepositoryImpl.class);
  private static final String PROGRESS_COLUMN_ID = "p.id";
  private static final String PROGRESS_COLUMN_COUNT = "cast(count(distinct m.id) as decimal)/greatest(cast(count(distinct l.id)*count(distinct k.id) as decimal), 1)";

  private final LocaleRepository localeRepository;
  private final KeyRepository keyRepository;

  @Inject
  public ProjectRepositoryImpl(
          Persistence persistence,
          Validator validator,
          LocaleRepository localeRepository,
          KeyRepository keyRepository) {
    super(persistence, validator);

    this.localeRepository = localeRepository;
    this.keyRepository = keyRepository;
  }

  @Override
  public PagedList<Project> findBy(ProjectCriteria criteria) {
    ExpressionList<Project> query = createQuery(criteria).where();

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

    if (criteria.hasFetch(FETCH_MYROLE) && criteria.getLoggedInUserId() != null) {
      Map<UUID, ProjectRole> roleMap = roles(paged.getList().stream().map(Project::getId).collect(toList()), criteria.getLoggedInUserId());

      paged.getList()
              .forEach(p -> p.myRole = roleMap.getOrDefault(p.id, null));
    }

    return paged;
  }

  @Override
  protected Project fetch(Project project, ContextCriteria criteria) {
    if (criteria.hasFetch(FETCH_PROGRESS)) {
      Map<UUID, Double> progressMap = progress(singletonList(project.id));

      project.progress = progressMap.getOrDefault(project.id, 0.0);
    }

    if (criteria.hasFetch(FETCH_MYROLE) && criteria.getLoggedInUserId() != null) {
      Map<UUID, ProjectRole> roleMap = roles(singletonList(project.id), criteria.getLoggedInUserId());

      project.myRole = roleMap.getOrDefault(project.id, null);
    }

    return project;
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

    return byId(GetCriteria.from(id, null , fetches));
  }

  @Override
  public Project byOwnerAndName(String username, String name, UUID loggedInUserId, String... fetches) {
    ContextCriteria criteria = new DefaultContextCriteria()
            .withLoggedInUserId(loggedInUserId)
            .withFetches(fetches);

    return fetch(
            createQuery(criteria)
                    .where()
                    .eq("owner.username", username)
                    .eq("name", name)
                    .findOne(),
            criteria
    );
  }

  @Override
  protected void preSave(Project t, boolean update) {
    persistence.markAsDirty(t);
    if (t.members == null) {
      t.members = new ArrayList<>();
    }
    if (t.members.isEmpty()) {
      t.members.add(new ProjectUser(ProjectRole.Owner).withProject(t).withUser(t.owner));
    }
  }

  @Override
  protected void postSave(Project t, boolean update) {
    super.postSave(t, update);
    persistence.refresh(t);
    persistence.refresh(t.owner);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Project t) {
    localeRepository.delete(t.locales);
    keyRepository.delete(t.keys);

    super.save(t.withName(String.format("%s-%s", t.id, t.name)).withDeleted(true));
  }

  @Override
  protected void preDelete(Project t) {
    super.preDelete(t);

    if (t == null || t.deleted) {
      throw new NotFoundException(dto.Project.class.getSimpleName(), t != null ? t.id : null);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Collection<Project> t) {
    keyRepository
            .delete(
                    t.stream().map(p -> p.keys).flatMap(Collection::stream).collect(toList()));
    localeRepository.delete(
            t.stream().map(p -> p.locales).flatMap(Collection::stream).collect(toList()));

    super.save(
            t.stream().map(p -> p.withName(String.format("%s-%s", p.id, p.name)).withDeleted(true))
                    .collect(toList()));
  }

  @Override
  protected Query<Project> createQuery(ContextCriteria criteria) {
    return createQuery(Project.class, PROPERTIES_TO_FETCH, FETCH_MAP, criteria.getFetches());
  }

  private Map<UUID, ProjectRole> roles(List<UUID> projectIds, UUID userId) {
    List<ProjectUser> results = log(
            () -> persistence.createQuery(ProjectUser.class)
                    .where()
                    .in("project_id", projectIds)
                    .eq("user_id", userId)
                    .findList(),
            LOGGER,
            "Retrieving project roles"
    );

    return results.stream().collect(toMap(r -> r.project.id, r -> r.role));
  }
}
