package services.impl;

import com.avaje.ebean.Ebean;
import criterias.MessageCriteria;
import models.Message;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;
import services.MessageService;

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

  @Inject
  public MessageServiceImpl(Validator validator, CacheService cache,
                            MessageRepository messageRepository, LogEntryService logEntryService,
                            AuthProvider authProvider) {
    super(validator, cache, messageRepository, Message::getCacheKey, logEntryService, authProvider);

    this.messageRepository = messageRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int countBy(Project project) {
    return log(
        () -> cache.getOrElse(
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
    return cache.getOrElse(
        String.format("project:id:%s:latest:messages:%d", project.id, limit),
        () -> messageRepository.latest(project, limit),
        60
    );
  }

  @Override
  protected void postCreate(Message t) {
    super.postCreate(t);

    // When message has been created, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.key.project.id));
    cache.removeByPrefix(Project.getCacheKey(t.key.project.owner.username, t.key.project.name));
    cache.removeByPrefix(String.format("message:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("locale:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("key:criteria:%s", t.key.project.id));
  }

  @Override
  protected void postUpdate(Message t) {
    super.postUpdate(t);

    cache.keys().forEach((key, value) -> LOGGER.debug("Key {} with expiration {}", key, value));
    cache.removeByPrefix(String.format("message:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("locale:criteria:%s", t.key.project.id));
    cache.removeByPrefix(String.format("key:criteria:%s", t.key.project.id));
  }
}
