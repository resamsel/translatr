package services.impl;

import static utils.Stopwatch.log;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSqlBuilder;
import com.fasterxml.jackson.databind.JsonNode;

import dto.PermissionException;
import models.ActionType;
import models.Key;
import models.LogEntry;
import models.Message;
import models.Project;
import models.ProjectRole;
import models.Stat;
import models.User;
import play.Configuration;
import services.KeyService;
import services.LogEntryService;
import services.MessageService;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
public class KeyServiceImpl extends AbstractModelService<Key, dto.Key> implements KeyService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceImpl.class);

  private final MessageService messageService;

  /**
   * 
   */
  @Inject
  public KeyServiceImpl(Configuration configuration, MessageService messageService,
      LogEntryService logEntryService) {
    super(dto.Key.class, configuration, logEntryService);
    this.messageService = messageService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Key byId(JsonNode id) {
    return Key.byId(UUID.fromString(id.asText()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Key toModel(dto.Key dto) {
    return dto.toModel(Project.byId(dto.projectId));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected dto.Key validate(dto.Key t) {
    if (t.name == null)
      throw new ValidationException("Field 'name' required");
    else if (Key.byProjectAndName(t.projectId, t.name) != null)
      throw new ValidationException(
          String.format("Key with name '%s' already exists in project '%s'", t.name, t.projectId));

    return t;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<UUID, Double> progress(List<UUID> keyIds, long localesSize) {
    List<Stat> stats = log(() -> Ebean.find(Stat.class)
        .setRawSql(
            RawSqlBuilder.parse("SELECT m.key_id, count(m.id) FROM message m GROUP BY m.key_id")
                .columnMapping("m.key_id", "id").columnMapping("count(m.id)", "count").create())
        .where().in("m.key_id", keyIds).findList(), LOGGER, "Retrieving key progress");

    return stats.stream().collect(Collectors.groupingBy(k -> k.id,
        Collectors.averagingDouble(t -> (double) t.count / (double) localesSize)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(Key t, boolean update) {
    if (update)
      logEntryService.save(LogEntry.from(ActionType.Update, t.project, dto.Key.class,
          dto.Key.from(Key.byId(t.id)), dto.Key.from(t)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Key t, boolean update) {
    if (!update)
      logEntryService
          .save(LogEntry.from(ActionType.Create, t.project, dto.Key.class, null, dto.Key.from(t)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preDelete(Key t) {
    if (!t.project.hasPermissionAny(User.loggedInUser(), ProjectRole.Owner, ProjectRole.Developer))
      throw new PermissionException("User not allowed in project");

    logEntryService
        .save(LogEntry.from(ActionType.Delete, t.project, dto.Key.class, dto.Key.from(t), null));

    messageService.delete(Message.byKey(t));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preDelete(Collection<Key> t) {
    logEntryService.save(t.stream()
        .map(k -> LogEntry.from(ActionType.Delete, k.project, dto.Key.class, dto.Key.from(k), null))
        .collect(Collectors.toList()));

    messageService.delete(Message.byKeys(t.stream().map(k -> k.id).collect(Collectors.toList())));
  }
}
