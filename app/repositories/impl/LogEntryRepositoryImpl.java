package repositories.impl;

import actors.NotificationActorRef;
import actors.NotificationProtocol.PublishNotification;
import criterias.ContextCriteria;
import criterias.LogEntryCriteria;
import criterias.PagedListFactory;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import models.LogEntry;
import org.apache.commons.lang3.StringUtils;
import repositories.LogEntryRepository;
import repositories.Persistence;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.UUID;

@Singleton
public class LogEntryRepositoryImpl extends
        AbstractModelRepository<LogEntry, UUID, LogEntryCriteria> implements
        LogEntryRepository {
  private final NotificationActorRef notificationActor;

  @Inject
  public LogEntryRepositoryImpl(Persistence persistence, Validator validator, NotificationActorRef notificationActor) {
    super(persistence, validator);

    this.notificationActor = notificationActor;
  }

  @Override
  public PagedList<LogEntry> findBy(LogEntryCriteria criteria) {
    ExpressionList<LogEntry> query = findQuery(criteria);

    if (criteria.getTypes() != null && !criteria.getTypes().isEmpty()) {
      query.in("type", criteria.getTypes());
    }

    if (criteria.getOrder() != null) {
      query.order(criteria.getOrder());
    } else {
      query.order("whenCreated desc");
    }

    criteria.paged(query);

    return PagedListFactory.create(query.query().fetch("user").fetch("project"), criteria.hasFetch(FETCH_COUNT));
  }

  @Override
  public LogEntry byId(UUID id, String... fetches) {
    return QueryUtils.fetch(persistence.find(LogEntry.class).setId(id).setDisableLazyLoading(true),
            QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches)).findOne();
  }

  @Override
  public int countBy(LogEntryCriteria criteria) {
    return findQuery(criteria).findCount();
  }

  private ExpressionList<LogEntry> findQuery(LogEntryCriteria criteria) {
    ExpressionList<LogEntry> query = persistence.find(LogEntry.class).where();

    if (criteria.getIds() != null) {
      query.idIn(criteria.getIds());
    }

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.disjunction().ilike("before", "%" + criteria.getSearch() + "%")
              .ilike("after", "%" + criteria.getSearch() + "%").endJunction();
    }

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getProjectOwnerId() != null) {
      query.eq("project.owner.id", criteria.getProjectOwnerId());
    }

    if (criteria.getProjectMemberId() != null) {
      query.eq("project.members.user.id", criteria.getProjectMemberId());
    }

    return query;
  }

  @Override
  protected Query<LogEntry> createQuery(ContextCriteria criteria) {
    return createQuery(LogEntry.class, PROPERTIES_TO_FETCH, criteria.getFetches());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(LogEntry t, boolean update) {
    if (t.user != null && t.project != null) {
      notificationActor.tell(new PublishNotification(t), null);
    }
  }
}
