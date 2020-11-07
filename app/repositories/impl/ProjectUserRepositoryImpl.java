package repositories.impl;

import actors.NotificationActorRef;
import actors.NotificationProtocol.FollowNotification;
import criterias.ContextCriteria;
import criterias.PagedListFactory;
import criterias.ProjectUserCriteria;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import models.ProjectUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.PagedListFactoryProvider;
import repositories.Persistence;
import repositories.ProjectUserRepository;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import static utils.Stopwatch.log;

@Singleton
public class ProjectUserRepositoryImpl extends
        AbstractModelRepository<ProjectUser, Long, ProjectUserCriteria> implements
        ProjectUserRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUserRepositoryImpl.class);

  private final NotificationActorRef notificationActor;
  private final PagedListFactory pagedListFactory;

  @Inject
  public ProjectUserRepositoryImpl(
          Persistence persistence,
          Validator validator,
          NotificationActorRef notificationActor,
          PagedListFactoryProvider pagedListFactoryProvider) {
    super(persistence, validator);

    this.notificationActor = notificationActor;
    this.pagedListFactory = pagedListFactoryProvider.get();
  }

  @Override
  public PagedList<ProjectUser> findBy(ProjectUserCriteria criteria) {
    return log(() -> pagedListFactory.createPagedList(findQuery(criteria).query()), LOGGER,
            "findBy");
  }

  @Override
  public ProjectUser byId(Long id, String... propertiesToFetch) {
    return QueryUtils.fetch(persistence.find(ProjectUser.class), PROPERTIES_TO_FETCH, FETCH_MAP).where().idEq(id)
            .findOne();
  }

  @Override
  public int countBy(ProjectUserCriteria criteria) {
    return log(() -> findQuery(criteria).findCount(), LOGGER, "countBy");
  }

  private ExpressionList<ProjectUser> findQuery(ProjectUserCriteria criteria) {
    ExpressionList<ProjectUser> query = createQuery(criteria).where();

    query.eq("project.deleted", false);
    query.eq("user.active", true);

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (!criteria.getRoles().isEmpty()) {
      query.in("role", criteria.getRoles());
    }

    if (criteria.getSearch() != null) {
      query.disjunction()
              .ilike("user.name", "%" + criteria.getSearch() + "%")
              .ilike("user.username", "%" + criteria.getSearch() + "%")
              .endJunction();
    }

    criteria.paged(query);

    return query;
  }

  @Override
  protected Query<ProjectUser> createQuery(ContextCriteria criteria) {
    return createQuery(ProjectUser.class, PROPERTIES_TO_FETCH, FETCH_MAP, criteria.getFetches());
  }

  @Override
  protected void postSave(ProjectUser t, boolean update) {
    if (!update) {
      persistence.refresh(t.user);
    }

    notificationActor.tell(new FollowNotification(t.user.id, t.project.id), null);
  }
}
