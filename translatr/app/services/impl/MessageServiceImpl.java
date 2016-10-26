package services.impl;

import static utils.Stopwatch.log;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

import models.ActionType;
import models.LogEntry;
import models.Message;
import models.Project;
import play.Configuration;
import play.cache.CacheApi;
import play.libs.Json;
import services.LogEntryService;
import services.MessageService;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class MessageServiceImpl extends AbstractModelService<Message> implements MessageService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

  private final CacheApi cache;

  /**
   * 
   */
  @Inject
  public MessageServiceImpl(Configuration configuration, CacheApi cache,
      LogEntryService logEntryService) {
    super(configuration, logEntryService);
    this.cache = cache;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Message create(JsonNode json) {
    Message message = null;

    if (json.hasNonNull("id")) {
      message = Message.byId(UUID.fromString(json.get("id").asText()));
      message.updateFrom(Json.fromJson(json, Message.class));
    } else {
      message = Json.fromJson(json, Message.class);
      LOGGER.debug("Locale: {}", Json.toJson(message));
    }

    save(message);

    return message;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int countBy(Project project) {
    return log(() -> cache.getOrElse(String.format("message:countByProject:%s", project.id),
        () -> Message.countByUncached(project), 60), LOGGER, "countByProject");
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
    Map<UUID, Message> messages =
        Message.byIds(t.stream().map(m -> m.id).collect(Collectors.toList()));

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
    if (!update)
      logEntryService.save(logEntryCreate(t));
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
