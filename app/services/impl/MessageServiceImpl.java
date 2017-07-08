package services.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.Stopwatch.log;

import actors.MessageWordCountActor;
import actors.WordCountProtocol.ChangeMessageWordCount;
import akka.actor.ActorRef;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
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
import models.LogEntry;
import models.Message;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import services.LogEntryService;
import services.MessageService;
import utils.MessageUtils;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class MessageServiceImpl extends AbstractModelService<Message, UUID, MessageCriteria>
    implements MessageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

  private final ActorRef messageWordCountActor;

  private final CacheApi cache;

  @Inject
  public MessageServiceImpl(Validator validator, CacheApi cache, LogEntryService logEntryService,
      @Named(MessageWordCountActor.NAME) ActorRef messageWordCountActor) {
    super(validator, logEntryService);

    this.cache = cache;
    this.messageWordCountActor = messageWordCountActor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<Message> findBy(MessageCriteria criteria) {
    return Message.findBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Message byId(UUID id, String... fetches) {
    return cache.getOrElse(Message.getCacheKey(id, fetches), () -> Message.byId(id, fetches), 10);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int countBy(Project project) {
    return log(() -> cache.getOrElse(String.format("message:countByProject:%s", project.id),
        () -> Message.countBy(project), 60), LOGGER, "countBy");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    try {
      Ebean
          .createSqlUpdate(
              "update message set word_count = null from locale where message.locale_id = locale.id and locale.project_id = :projectId")
          .setParameter("projectId", projectId).execute();
    } catch (Exception e) {
      LOGGER.error("Error while resetting word count", e);
    }
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
      Message existing = Message.byId(t.id);
      if (!Objects.equals(t.value, existing.value))
      // Only track changes of message´s value
      {
        logEntryService.save(logEntryUpdate(t, existing));
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
    Map<UUID, Message> messages = ids.size() > 0 ? Message.byIds(ids) : Collections.emptyMap();

    logEntryService.save(t.stream().filter(
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
      logEntryService.save(logEntryCreate(t));

      // When message has been created, the project cache needs to be invalidated
      cache.remove(Project.getCacheKey(t.key.project.id));
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
    logEntryService.save(LogEntry.from(ActionType.Delete, t.key.project, dto.Message.class,
        dto.Message.from(t), null));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preDelete(Collection<Message> t) {
    logEntryService.save(t.stream().map(m -> LogEntry.from(ActionType.Delete, m.key.project,
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
        t.stream().filter(m -> m.wordCount != null).map(m -> {
          return new ChangeMessageWordCount(m.id, m.locale.project.id, m.locale.id, m.key.id, 0,
              -m.wordCount);
        }).collect(toMap(wc -> wc.messageId, wc -> wc, (a, b) -> a));

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
