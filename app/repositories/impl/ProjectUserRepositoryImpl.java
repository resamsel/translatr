package repositories.impl;

import static utils.Stopwatch.log;

import actors.NotificationActor;
import actors.NotificationProtocol.FollowNotification;
import akka.actor.ActorRef;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import criterias.HasNextPagedList;
import criterias.ProjectUserCriteria;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.ActionType;
import models.LogEntry;
import models.ProjectUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LogEntryRepository;
import repositories.ProjectUserRepository;
import utils.QueryUtils;

@Singleton
public class ProjectUserRepositoryImpl extends
    AbstractModelRepository<ProjectUser, Long, ProjectUserCriteria> implements
    ProjectUserRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUserRepositoryImpl.class);

  private final Find<Long, ProjectUser> find = new Find<Long, ProjectUser>() {
  };

  private final ActorRef notificationActor;

  @Inject
  public ProjectUserRepositoryImpl(Validator validator, LogEntryRepository logEntryRepository,
      @Named(NotificationActor.NAME) ActorRef notificationActor) {
    super(validator, logEntryRepository);

    this.notificationActor = notificationActor;
  }

  @Override
  public PagedList<ProjectUser> findBy(ProjectUserCriteria criteria) {
    return log(() -> HasNextPagedList.create(findQuery(criteria).query().fetch("user")), LOGGER,
        "findBy");
  }

  @Override
  public ProjectUser byId(Long id, String... propertiesToFetch) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where().idEq(id)
        .findUnique();
  }

  @Override
  public int countBy(ProjectUserCriteria criteria) {
    return log(() -> findQuery(criteria).findCount(), LOGGER, "countBy");
  }

  private ExpressionList<ProjectUser> findQuery(ProjectUserCriteria criteria) {
    ExpressionList<ProjectUser> query = find.where();

    query.eq("project.deleted", false);

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    criteria.paged(query);

    return query;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void prePersist(ProjectUser t, boolean update) {
    if (update) {
      logEntryRepository.save(LogEntry.from(ActionType.Update, t.project, dto.ProjectUser.class,
          toDto(byId(t.id)), toDto(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(ProjectUser t, boolean update) {
    if (!update) {
      logEntryRepository
          .save(LogEntry.from(ActionType.Create, t.project, dto.ProjectUser.class, null, toDto(t)));
    }

    notificationActor.tell(new FollowNotification(t.user.id, t.project.id), null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(ProjectUser t) {
    logEntryRepository
        .save(LogEntry.from(ActionType.Delete, t.project, dto.ProjectUser.class, toDto(t), null));
  }

  private dto.ProjectUser toDto(ProjectUser t) {
    return dto.ProjectUser.from(t);
  }
}
