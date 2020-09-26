package repositories.impl;

import actors.MessageWordCountActorRef;
import criterias.ContextCriteria;
import criterias.MessageCriteria;
import criterias.PagedListFactory;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import mappers.MessageMapper;
import models.Key;
import models.Message;
import models.Project;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import repositories.Persistence;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static utils.Stopwatch.log;

@Singleton
public class MessageRepositoryImpl extends
        AbstractModelRepository<Message, UUID, MessageCriteria> implements
        MessageRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageRepositoryImpl.class);

  private final MessageWordCountActorRef messageWordCountActor;
  private final MessageMapper messageMapper;

  @Inject
  public MessageRepositoryImpl(
          Persistence persistence,
          Validator validator,
          MessageWordCountActorRef messageWordCountActor,
          MessageMapper messageMapper) {
    super(persistence, validator);

    this.messageWordCountActor = messageWordCountActor;
    this.messageMapper = messageMapper;
  }

  @Override
  public PagedList<Message> findBy(MessageCriteria criteria) {
    ExpressionList<Message> query = fetch(criteria.getFetches()).where();

    if (criteria.getProjectId() != null) {
      query.eq("key.project.id", criteria.getProjectId());
    }

    if (criteria.getLocaleId() != null) {
      query.eq("locale.id", criteria.getLocaleId());
    }

    if (criteria.getLocaleIds() != null) {
      query.in("locale.id", criteria.getLocaleIds());
    }

    if (StringUtils.isNotEmpty(criteria.getKeyName())) {
      query.eq("key.name", criteria.getKeyName());
    }

    if (criteria.getKeyIds() != null) {
      query.in("key.id", criteria.getKeyIds());
    }

    if (StringUtils.isNotEmpty(criteria.getSearch())) {
      query.ilike("value", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return log(() -> PagedListFactory.create(query), LOGGER, "findBy");
  }

  @Override
  public Message byId(UUID id, String... fetches) {
    return QueryUtils.fetch(
            persistence.find(Message.class).setId(id).setDisableLazyLoading(true),
            QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches),
            FETCH_MAP)
            .findOne();
  }

  @Override
  public Map<UUID, Message> byIds(List<UUID> ids) {
    return QueryUtils.fetch(persistence.find(Message.class), PROPERTIES_TO_FETCH, FETCH_MAP).where().idIn(ids)
            .findMap();
  }

  @Override
  protected Query<Message> createQuery(ContextCriteria criteria) {
    return fetch(criteria.getFetches());
  }

  private Query<Message> fetch(String... fetches) {
    return fetch(fetches != null ? Arrays.asList(fetches) : Collections.emptyList());
  }

  private Query<Message> fetch(List<String> fetches) {
    return QueryUtils.fetch(persistence.find(Message.class).alias("k").setDisableLazyLoading(true),
            QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP);
  }

  @Override
  public int countBy(Project project) {
    return log(() -> persistence.find(Message.class).where().eq("key.project", project).findCount(), LOGGER,
            "countBy(Project)");
  }

  @Override
  public List<Message> byLocale(UUID localeId) {
    return fetch().where().eq("locale.id", localeId).findList();
  }

  @Override
  public List<Message> byLocales(Collection<UUID> ids) {
    return fetch().where().in("locale.id", ids).findList();
  }

  @Override
  public List<Message> byKey(Key key) {
    return fetch().where().eq("key", key).findList();
  }

  @Override
  public List<Message> byKeys(Collection<UUID> ids) {
    return fetch().where().in("key.id", ids).findList();
  }
}
