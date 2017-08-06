package repositories.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.Stopwatch.log;

import actors.MessageWordCountActor;
import actors.WordCountProtocol.ChangeMessageWordCount;
import akka.actor.ActorRef;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import criterias.HasNextPagedList;
import criterias.MessageCriteria;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.ActionType;
import models.Key;
import models.LogEntry;
import models.Message;
import models.Project;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LogEntryRepository;
import repositories.MessageRepository;
import utils.MessageUtils;
import utils.QueryUtils;

@Singleton
public class MessageRepositoryImpl extends
    AbstractModelRepository<Message, UUID, MessageCriteria> implements
    MessageRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageRepositoryImpl.class);

  private final ActorRef messageWordCountActor;

  private final Find<UUID, Message> find = new Find<UUID, Message>() {
  };

  @Inject
  public MessageRepositoryImpl(Validator validator, LogEntryRepository logEntryRepository,
      @Named(MessageWordCountActor.NAME) ActorRef messageWordCountActor) {
    super(validator, logEntryRepository);

    this.messageWordCountActor = messageWordCountActor;
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

    if (StringUtils.isNotEmpty(criteria.getSearch())) {
      query.ilike("value", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return log(() -> HasNextPagedList.create(query), LOGGER, "findBy");
  }

  @Override
  public Message byId(UUID id, String... fetches) {
    return QueryUtils
        .fetch(find.setId(id), QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP)
        .findUnique();
  }

  @Override
  public Map<UUID, Message> byIds(List<UUID> ids) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where().idIn(ids)
        .findMap();
  }

  private Query<Message> fetch(List<String> fetches) {
    return fetch(fetches.toArray(new String[fetches.size()]));
  }

  private Query<Message> fetch(String... fetches) {
    return QueryUtils.fetch(find.query().alias("k").setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP);
  }

  @Override
  public int countBy(Project project) {
    return log(() -> find.where().eq("key.project", project).findCount(), LOGGER,
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

  @Override
  public List<Message> latest(Project project, int limit) {
    return fetch().where().eq("key.project", project).order("whenUpdated desc").setMaxRows(limit)
        .findList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preSave(Message t, boolean update) {
    int wordCount = t.wordCount != null ? t.wordCount : 0;
    t.wordCount = MessageUtils.wordCount(t.value);

    messageWordCountActor.tell(new ChangeMessageWordCount(t.id, t.locale.project.id, t.locale.id,
        t.key.id, t.wordCount, t.wordCount - wordCount), null);
  }

  @Override
  protected void prePersist(Message t, boolean update) {
    if (update) {
      Message existing = byId(t.id);
      if (!Objects.equals(t.value, existing.value))
      // Only track changes of message´s value
      {
        logEntryRepository.save(logEntryUpdate(t, existing));
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(Collection<Message> t) {
    Map<UUID, ChangeMessageWordCount> wordCount = t.stream().filter(m -> m.id != null).map(m -> {
      int wc = MessageUtils.wordCount(m);
      return new ChangeMessageWordCount(m.id, m.locale.project.id, m.locale.id, m.key.id, wc,
          wc - (m.wordCount != null ? m.wordCount : 0));
    }).collect(toMap(wc -> wc.messageId, wc -> wc, (a, b) -> a));

    messageWordCountActor.tell(wordCount.values(), null);

    // Update model
    t.stream().filter(m -> m.id != null).forEach(m -> m.wordCount = wordCount.getOrDefault(m.id,
        new ChangeMessageWordCount(null, null, null, null, 0, 0)).wordCount);

    List<UUID> ids = t.stream().filter(m -> m.id != null).map(m -> m.id).collect(toList());
    Map<UUID, Message> messages = ids.size() > 0 ? byIds(ids) : Collections.emptyMap();

    logEntryRepository.save(t.stream().filter(
        // Only track changes of message´s value
        m -> Ebean.getBeanState(m).isNew() || !Objects.equals(m.value, messages.get(m.id).value))
        .map(m -> Ebean.getBeanState(m).isNew() ? logEntryCreate(m)
            : logEntryUpdate(m, messages.get(m.id)))
        .collect(toList()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Message t, boolean update) {
    if (!update) {
      logEntryRepository.save(logEntryCreate(t));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Collection<Message> t) {
    List<Message> noWordCountMessages =
        t.stream().filter(m -> m.wordCount == null).collect(toList());
    Map<UUID, ChangeMessageWordCount> wordCount = noWordCountMessages.stream().map(m -> {
      int wc = MessageUtils.wordCount(m);
      return new ChangeMessageWordCount(m.id, m.locale.project.id, m.locale.id, m.key.id, wc,
          wc - (m.wordCount != null ? m.wordCount : 0));
    }).collect(toMap(wc -> wc.messageId, wc -> wc));

    messageWordCountActor.tell(wordCount.values(), null);

    // Update model
    noWordCountMessages.stream().filter(m -> m.id != null).forEach(m -> m.wordCount = wordCount
        .getOrDefault(m.id, new ChangeMessageWordCount(null, null, null, null, 0, 0)).wordCount);

    try {
      persist(noWordCountMessages);
    } catch (Exception e) {
      LOGGER.error("Error while persisting word count", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(Message t) {
    logEntryRepository.save(LogEntry.from(ActionType.Delete, t.key.project, dto.Message.class,
        dto.Message.from(t), null));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preDelete(Collection<Message> t) {
    logEntryRepository.save(t.stream().map(m -> LogEntry.from(ActionType.Delete, m.key.project,
        dto.Message.class, dto.Message.from(m), null)).collect(toList()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Message t) {
    if (t.wordCount != null) {
      messageWordCountActor.tell(new ChangeMessageWordCount(t.id, t.locale.project.id, t.locale.id,
          t.key.id, 0, -t.wordCount), null);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Collection<Message> t) {
    Map<UUID, ChangeMessageWordCount> wordCount =
        t.stream()
            .filter(m -> m.wordCount != null)
            .map(
                m -> new ChangeMessageWordCount(m.id, m.locale.project.id, m.locale.id, m.key.id, 0,
                    -m.wordCount))
            .collect(toMap(wc -> wc.messageId, wc -> wc, (a, b) -> a));

    messageWordCountActor.tell(wordCount.values(), null);
  }

  private LogEntry logEntryCreate(Message message) {
    return LogEntry.from(ActionType.Create, message.key.project, dto.Message.class, null,
        dto.Message.from(message));
  }

  private LogEntry logEntryUpdate(Message message, Message previous) {
    return LogEntry.from(ActionType.Update, message.key.project, dto.Message.class,
        dto.Message.from(previous), dto.Message.from(message));
  }
}
