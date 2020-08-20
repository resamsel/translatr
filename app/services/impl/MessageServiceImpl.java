package services.impl;

import io.ebean.Ebean;
import io.ebean.PagedList;
import criterias.MessageCriteria;
import models.ActionType;
import models.Message;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import services.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.List;
import java.util.UUID;

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

  @Inject
  public MessageServiceImpl(Validator validator, CacheService cache,
                            MessageRepository messageRepository, LogEntryService logEntryService,
                            AuthProvider authProvider, MetricService metricService) {
    super(validator, cache, messageRepository, Message::getCacheKey, logEntryService, authProvider);

    this.messageRepository = messageRepository;
    this.metricService = metricService;
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
      Ebean
          .createSqlUpdate(
              "update message set word_count = null from locale where message.locale_id = locale.id and locale.project_id = :projectId")
          .setParameter("projectId", projectId).execute();
    } catch (Exception e) {
      LOGGER.error("Error while resetting word count", e);
    }
  }

  @Override
  public List<Message> latest(Project project, int limit) {
    return cache.getOrElseUpdate(
            String.format("project:id:%s:latest:messages:%d", project.id, limit),
            () -> messageRepository.latest(project, limit),
            60
    );
  }

  @Override
  protected PagedList<Message> postFind(PagedList<Message> pagedList) {
    metricService.logEvent(Message.class, ActionType.Read);

    return super.postFind(pagedList);
  }

  @Override
  protected Message postGet(Message message) {
    metricService.logEvent(Message.class, ActionType.Read);

    return super.postGet(message);
  }

  @Override
  protected void postCreate(Message t) {
    super.postCreate(t);

    metricService.logEvent(Message.class, ActionType.Create);

    // When message has been created, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.key.project.id));
    cache.removeByPrefix(Project.getCacheKey(t.key.project.owner.username, t.key.project.name));
    cache.removeByPrefix(String.format("message:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("locale:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("key:criteria:%s", t.key.project.id));
  }

  @Override
  protected Message postUpdate(Message t) {
    super.postUpdate(t);

    metricService.logEvent(Message.class, ActionType.Update);

    cache.keys().forEach((key, value) -> LOGGER.debug("Key {} with expiration {}", key, value));
    cache.removeByPrefix(String.format("message:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("locale:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("key:criteria:%s", t.key.project.id));

    return t;
  }

  @Override
  protected void postDelete(Message t) {
    super.postDelete(t);

    metricService.logEvent(Message.class, ActionType.Delete);
  }
}
