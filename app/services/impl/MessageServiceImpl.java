package services.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol;
import actors.MessageWordCountActorRef;
import actors.WordCountProtocol;
import criterias.GetCriteria;
import criterias.MessageCriteria;
import io.ebean.PagedList;
import mappers.MessageMapper;
import models.ActionType;
import models.Message;
import models.Project;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.MessageRepository;
import repositories.Persistence;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;
import services.MessageService;
import services.MetricService;
import utils.MessageUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.Stopwatch.log;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class MessageServiceImpl extends AbstractModelService<Message, UUID, MessageCriteria>
        implements MessageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

  private final MessageRepository messageRepository;
  private final MetricService metricService;
  private final MessageWordCountActorRef messageWordCountActor;
  private final Persistence persistence;
  private final MessageMapper messageMapper;

  @Inject
  public MessageServiceImpl(
          Validator validator,
          CacheService cache,
          MessageRepository messageRepository,
          LogEntryService logEntryService,
          AuthProvider authProvider,
          MetricService metricService,
          ActivityActorRef activityActor,
          MessageWordCountActorRef messageWordCountActor,
          Persistence persistence,
          MessageMapper messageMapper) {
    super(validator, cache, messageRepository, Message::getCacheKey, logEntryService, authProvider, activityActor);

    this.messageRepository = messageRepository;
    this.metricService = metricService;
    this.messageWordCountActor = messageWordCountActor;
    this.persistence = persistence;
    this.messageMapper = messageMapper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int countBy(Project project) {
    return log(
            () -> cache.getOrElseUpdate(
                    String.format("project:id:%s:message:countByProject", project.id),
                    () -> messageRepository.countBy(project),
                    60),
            LOGGER,
            "countBy"
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    try {
      persistence
              .createSqlUpdate(
                      "update message set word_count = null from locale where message.locale_id = locale.id and locale.project_id = :projectId")
              .setParameter("projectId", projectId).execute();
    } catch (Exception e) {
      LOGGER.error("Error while resetting word count", e);
    }
  }

  @Override
  protected PagedList<Message> postFind(PagedList<Message> pagedList, Http.Request request) {
    metricService.logEvent(Message.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  @Override
  protected Message postGet(Message message, Http.Request request) {
    metricService.logEvent(Message.class, ActionType.Read);

    return super.postGet(message, request);
  }

  @Override
  protected void postCreate(Message t, Http.Request request) {
    super.postCreate(t, request);

    metricService.logEvent(Message.class, ActionType.Create);

    // When message has been created, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.key.project.id));
    cache.removeByPrefix(Project.getCacheKey(t.key.project.owner.username, t.key.project.name));
    cache.removeByPrefix(String.format("message:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("locale:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("key:criteria:%s", t.key.project.id));

    activityActor.tell(logEntryCreate(t, authProvider.loggedInUser(request)), null);
  }

  @Override
  protected Message postUpdate(Message t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(Message.class, ActionType.Update);

    cache.keys().forEach((key, value) -> LOGGER.debug("Key {} with expiration {}", key, value));
    cache.removeByPrefix(String.format("message:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("locale:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("key:criteria:%s", t.key.project.id));

    return t;
  }

  @Override
  protected void postDelete(Message t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(Message.class, ActionType.Delete);

    if (t.wordCount != null) {
      messageWordCountActor.tell(new WordCountProtocol.ChangeMessageWordCount(t.id, t.locale.project.id, t.locale.id,
              t.key.id, 0, -t.wordCount, request), null);
    }
  }

  @Override
  protected void preUpdate(Message t, Http.Request request) {
    super.preUpdate(t, request);

    Message existing = byId(GetCriteria.from(t.id, request));
    if (!Objects.equals(t.value, existing.value)) {
      // Only track changes of message´s value
      activityActor.tell(logEntryUpdate(t, existing, authProvider.loggedInUser(request)), null);
    }
  }

  @Override
  protected void preSave(Message t, Http.Request request) {
    super.preSave(t, request);

    int wordCount = t.wordCount != null ? t.wordCount : 0;
    t.wordCount = MessageUtils.wordCount(t.value);

    messageWordCountActor.tell(new WordCountProtocol.ChangeMessageWordCount(t.id, t.locale.project.id, t.locale.id,
            t.key.id, t.wordCount, t.wordCount - wordCount, request), null);
  }

  @Override
  protected void preSave(Collection<Message> t, Http.Request request) {
    super.preSave(t, request);

    Map<UUID, WordCountProtocol.ChangeMessageWordCount> wordCount = t.stream()
            .filter(m -> m.id != null)
            .map(m -> {
              int wc = MessageUtils.wordCount(m);
              return new WordCountProtocol.ChangeMessageWordCount(
                      m.id,
                      m.locale.project.id,
                      m.locale.id,
                      m.key.id,
                      wc,
                      wc - (m.wordCount != null ? m.wordCount : 0),
                      request);
            })
            .collect(toMap(wc -> wc.messageId, wc -> wc, (a, b) -> a));

    messageWordCountActor.tell(wordCount.values(), null);

    // Update model
    t.stream()
            .filter(m -> m.id != null)
            .forEach(m -> m.wordCount = wordCount.getOrDefault(m.id,
                    new WordCountProtocol.ChangeMessageWordCount(null, null, null, null, 0, 0, request)).wordCount);
    t.stream()
            .filter(m -> m.id == null)
            .forEach(m -> m.wordCount = MessageUtils.wordCount(m));

    List<UUID> ids = t.stream().filter(m -> m.id != null).map(m -> m.id).collect(toList());
    Map<UUID, Message> messages = ids.size() > 0 ? messageRepository.byIds(ids) : Collections.emptyMap();
    User loggedInUser = authProvider.loggedInUser(request);

    activityActor.tell(
            new ActivityProtocol.Activities<>(t.stream().filter(
                    // Only track changes of message´s value
                    m -> persistence.isNew(m) || !Objects
                            .equals(m.value, messages.get(m.id).value))
                    .map(m -> persistence.isNew(m) ? logEntryCreate(m, loggedInUser)
                            : logEntryUpdate(m, messages.get(m.id), loggedInUser))
                    .collect(toList())),
            null
    );
  }

  @Override
  protected void postSave(Collection<Message> t, Http.Request request) {
    super.postSave(t, request);

    List<Message> noWordCountMessages =
            t.stream().filter(m -> m.wordCount == null).collect(toList());
    Map<UUID, WordCountProtocol.ChangeMessageWordCount> wordCount = noWordCountMessages.stream().map(m -> {
      int wc = MessageUtils.wordCount(m);
      return new WordCountProtocol.ChangeMessageWordCount(m.id, m.locale.project.id, m.locale.id, m.key.id, wc,
              wc - (m.wordCount != null ? m.wordCount : 0), request);
    }).collect(toMap(wc -> wc.messageId, wc -> wc));

    messageWordCountActor.tell(wordCount.values(), null);

    // Update model
    noWordCountMessages.stream()
            .filter(m -> m.id != null)
            .forEach(m -> m.wordCount = wordCount.getOrDefault(m.id, createWordCount(request)).wordCount);

    if (noWordCountMessages.size() > 0) {
      try {
        save(noWordCountMessages, request);
      } catch (Exception e) {
        LOGGER.error("Error while persisting word count", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(Message t, Http.Request request) {
    super.preDelete(t, request);

    activityActor.tell(
            new ActivityProtocol.Activity<>(ActionType.Delete, authProvider.loggedInUser(request), t.key.project, dto.Message.class, messageMapper.toDto(t, request),
                    null),
            null
    );
  }

  @Override
  protected void preDelete(Collection<Message> t, Http.Request request) {
    super.preDelete(t, request);

    activityActor.tell(
            new ActivityProtocol.Activities<>(t.stream().map(m -> new ActivityProtocol.Activity<>(ActionType.Delete, authProvider.loggedInUser(request), m.key.project,
                    dto.Message.class, messageMapper.toDto(m, request), null)).collect(toList())),
            null
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Collection<Message> t, Http.Request request) {
    super.postDelete(t, request);

    Map<UUID, WordCountProtocol.ChangeMessageWordCount> wordCount =
            t.stream()
                    .filter(m -> m.wordCount != null)
                    .map(
                            m -> new WordCountProtocol.ChangeMessageWordCount(m.id, m.locale.project.id, m.locale.id, m.key.id, 0,
                                    -m.wordCount, request))
                    .collect(toMap(wc -> wc.messageId, wc -> wc, (a, b) -> a));

    messageWordCountActor.tell(wordCount.values(), null);
  }

  private ActivityProtocol.Activity<dto.Message> logEntryCreate(Message message, User loggedInUser) {
    return new ActivityProtocol.Activity<>(
            ActionType.Create,
            loggedInUser,
            message.key.project,
            dto.Message.class,
            null,
            messageMapper.toDto(message, null)
    );
  }

  private ActivityProtocol.Activity<dto.Message> logEntryUpdate(Message message, Message previous, User loggedInUser) {
    return new ActivityProtocol.Activity<>(
            ActionType.Update,
            loggedInUser,
            message.key.project,
            dto.Message.class,
            messageMapper.toDto(previous, null),
            messageMapper.toDto(message, null)
    );
  }

  private WordCountProtocol.ChangeMessageWordCount createWordCount(Http.Request request) {
    return new WordCountProtocol.ChangeMessageWordCount(null, null, null, null, 0, 0, request);
  }
}
