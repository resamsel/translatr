package repositories.impl;

import actors.ActivityActor;
import actors.ActivityProtocol.Activity;
import actors.NotificationActor;
import actors.NotificationProtocol.FollowNotification;
import akka.actor.ActorRef;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import criterias.PagedListFactory;
import criterias.ProjectUserCriteria;
import mappers.ProjectUserMapper;
import models.ActionType;
import models.ProjectUser;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.PagedListFactoryProvider;
import repositories.ProjectUserRepository;
import repositories.RepositoryProvider;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validator;

import static utils.Stopwatch.log;

@Singleton
public class ProjectUserRepositoryImpl extends
    AbstractModelRepository<ProjectUser, Long, ProjectUserCriteria> implements
    ProjectUserRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUserRepositoryImpl.class);

  private final Find<Long, ProjectUser> repository;
  private final ActorRef notificationActor;
  private final PagedListFactory pagedListFactory;

  @Inject
  public ProjectUserRepositoryImpl(Validator validator,
                                   @Named(ActivityActor.NAME) ActorRef activityActor,
                                   @Named(NotificationActor.NAME) ActorRef notificationActor,
                                   RepositoryProvider repositoryProvider,
                                   PagedListFactoryProvider pagedListFactoryProvider) {
    super(validator, activityActor);

    this.repository = repositoryProvider.getProjectUserRepository();
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
    return QueryUtils.fetch(repository.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where().idEq(id)
        .findUnique();
  }

  @Override
  public int countBy(ProjectUserCriteria criteria) {
    return log(() -> findQuery(criteria).findCount(), LOGGER, "countBy");
  }

  private ExpressionList<ProjectUser> findQuery(ProjectUserCriteria criteria) {
    ExpressionList<ProjectUser> query = QueryUtils
        .fetch(repository.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where();

    query.eq("project.deleted", false);

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (!criteria.getRoles().isEmpty()) {
      query.in("role", criteria.getRoles());
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
      activityActor.tell(
          new Activity<>(
              ActionType.Update, User.loggedInUser(), t.project, dto.ProjectUser.class, toDto(byId(t.id)), toDto(t)
          ),
          null
      );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(ProjectUser t, boolean update) {
    if (!update) {
      Ebean.refresh(t.user);
      activityActor.tell(
          new Activity<>(
              ActionType.Create, User.loggedInUser(), t.project, dto.ProjectUser.class, null, toDto(t)
          ),
          null
      );
    }

    notificationActor.tell(new FollowNotification(t.user.id, t.project.id), null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(ProjectUser t) {
    activityActor.tell(
        new Activity<>(
            ActionType.Delete, User.loggedInUser(), t.project, dto.ProjectUser.class, toDto(t), null
        ),
        null
    );
  }

  private dto.ProjectUser toDto(ProjectUser t) {
    return ProjectUserMapper.toDto(t);
  }
}
