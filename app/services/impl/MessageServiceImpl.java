package services.impl;

import static utils.Stopwatch.log;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;

import criterias.MessageCriteria;
import models.ActionType;
import models.LogEntry;
import models.Message;
import models.Project;
import play.Configuration;
import play.cache.CacheApi;
import services.LogEntryService;
import services.MessageService;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class MessageServiceImpl extends AbstractModelService<Message, UUID, MessageCriteria>
    implements MessageService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

  private final CacheApi cache;

  /**
   * 
   */
  @Inject
  public MessageServiceImpl(Configuration configuration, Validator validator, CacheApi cache,
      LogEntryService logEntryService) {
    super(configuration, validator, logEntryService);
    this.cache = cache;
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
    return cache.getOrElse(Message.getCacheKey(id, fetches), () -> Message.byId(id, fetches), 60);
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
  public void preSave(Message t, boolean update) {
    if (update)
      logEntryService.save(logEntryUpdate(t, Message.byId(t.id)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(Collection<Message> t) {
    List<UUID> ids =
        t.stream().filter(m -> m.id != null).map(m -> m.id).collect(Collectors.toList());
    Map<UUID, Message> messages;
    if (ids.size() > 0)
      messages = Message.byIds(ids);
    else
      messages = Collections.emptyMap();

    logEntryService.save(t.stream().filter(m -> !Ebean.getBeanState(m).isNew()).map(m -> {
      return logEntryUpdate(m, messages.get(m.id));
    }).collect(Collectors.toList()));
    logEntryService.save(t.stream().filter(m -> Ebean.getBeanState(m).isNew())
        .map(m -> logEntryCreate(m)).collect(Collectors.toList()));
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
        dto.Message.class, dto.Message.from(m), null)).collect(Collectors.toList()));
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
