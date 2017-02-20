package services.impl;

import static utils.Stopwatch.log;

import java.util.Collection;
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
import com.avaje.ebean.RawSqlBuilder;

import criterias.LocaleCriteria;
import dto.PermissionException;
import models.ActionType;
import models.Locale;
import models.LogEntry;
import models.Message;
import models.Project;
import models.ProjectRole;
import models.Stat;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class LocaleServiceImpl extends AbstractModelService<Locale, UUID, LocaleCriteria>
    implements LocaleService {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceImpl.class);

  private final MessageService messageService;

  private final CacheApi cache;

  /**
   * 
   */
  @Inject
  public LocaleServiceImpl(Configuration configuration, Validator validator, CacheApi cache,
      MessageService messageService, LogEntryService logEntryService) {
    super(configuration, validator, logEntryService);
    this.cache = cache;
    this.messageService = messageService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<Locale> findBy(LocaleCriteria criteria) {
    return Locale.pagedBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Locale byId(UUID id) {
    return cache.getOrElse(Locale.getCacheKey(id), () -> Locale.byId(id), 60);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<UUID, Double> progress(List<UUID> localeIds, long keysSize) {
    List<Stat> stats = log(
        () -> Ebean.find(Stat.class)
            .setRawSql(RawSqlBuilder
                .parse("SELECT m.locale_id, count(m.id) FROM message m GROUP BY m.locale_id")
                .columnMapping("m.locale_id", "id").columnMapping("count(m.id)", "count").create())
            .where().in("m.locale_id", localeIds).findList(),
        LOGGER, "Retrieving locale progress");

    return stats.stream().collect(Collectors.groupingBy(k -> k.id,
        Collectors.averagingDouble(t -> (double) t.count / (double) keysSize)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(Locale t, boolean update) {
    if (update)
      logEntryService.save(LogEntry.from(ActionType.Update, t.project, dto.Locale.class,
          dto.Locale.from(byId(t.id)), dto.Locale.from(t)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Locale t, boolean update) {
    if (!update) {
      logEntryService.save(
          LogEntry.from(ActionType.Create, t.project, dto.Locale.class, null, dto.Locale.from(t)));

      // When message has been created, the project cache needs to be invalidated
      cache.remove(Project.getCacheKey(t.project.id));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(Locale t) {
    if (!t.project.hasPermissionAny(User.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager,
        ProjectRole.Translator))
      throw new PermissionException("User not allowed in project");

    logEntryService.save(
        LogEntry.from(ActionType.Delete, t.project, dto.Locale.class, dto.Locale.from(t), null));

    messageService.delete(Message.byLocale(t.id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Locale t) {
    // When message has been created, the project cache needs to be invalidated
    cache.remove(Project.getCacheKey(t.project.id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(Collection<Locale> t) {
    logEntryService.save(t.stream().map(l -> LogEntry.from(ActionType.Delete, l.project,
        dto.Locale.class, dto.Locale.from(l), null)).collect(Collectors.toList()));

    messageService
        .delete(Message.byLocales(t.stream().map(m -> m.id).collect(Collectors.toList())));
  }
}
