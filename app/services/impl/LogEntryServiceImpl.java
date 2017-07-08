package services.impl;

import static utils.Stopwatch.log;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import criterias.LogEntryCriteria;
import io.getstream.client.exception.StreamClientException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.AccessToken;
import models.Aggregate;
import models.LogEntry;
import models.Project;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import play.libs.concurrent.HttpExecutionContext;
import services.LogEntryService;
import services.NotificationService;
import utils.ContextKey;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class LogEntryServiceImpl extends AbstractModelService<LogEntry, UUID, LogEntryCriteria>
    implements LogEntryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryServiceImpl.class);

  private static final String H2_COLUMN_MILLIS =
      "datediff('millisecond', timestamp '1970-01-01 00:00:00', parsedatetime(formatdatetime(when_created, 'yyyy-MM-dd HH:00:00'), 'yyyy-MM-dd HH:mm:ss'))*1000";

  private final HttpExecutionContext executionContext;

  private final CacheApi cache;

  private final NotificationService notificationService;

  @Inject
  public LogEntryServiceImpl(HttpExecutionContext executionContext, Validator validator,
      CacheApi cache, NotificationService notificationService) {
    super(validator, null);
    this.executionContext = executionContext;
    this.cache = cache;
    this.notificationService = notificationService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogEntry byId(UUID id, String... fetches) {
    return cache.getOrElse(LogEntry.getCacheKey(id, fetches), () -> LogEntry.byId(id, fetches), 60);
  }

  @Override
  public List<Aggregate> getAggregates(LogEntryCriteria criteria) {
    ExpressionList<Aggregate> query =
        Ebean.find(Aggregate.class).setRawSql(getAggregatesRawSql()).where();

    if (criteria.getProjectId() != null) {
      query.eq("project_id", criteria.getProjectId());
    }

    if (criteria.getUserId() != null) {
      query.eq("user_id", criteria.getUserId());
    }

    String cacheKey =
        String.format("logentry:aggregates:%s:%s", criteria.getUserId(), criteria.getProjectId());

    // TODO: config cache duration
    return log(() -> cache.getOrElse(cacheKey, () -> query.findList(), 60), LOGGER,
        "Retrieving log entry aggregates");
  }

  /**
   * @return
   */
  private RawSql getAggregatesRawSql() {
    String dbpName = Ebean.getDefaultServer().getPluginApi().getDatabasePlatform().getName();
    if ("h2".equals(dbpName)) {
      return RawSqlBuilder
          .parse(String.format(
              "select %1$s as millis, count(*) as cnt from log_entry group by %1$s order by 1",
              H2_COLUMN_MILLIS))
          .columnMapping(H2_COLUMN_MILLIS, "millis").columnMapping("count(*)", "value").create();
    }

    return RawSqlBuilder
        .parse(
            "select when_created::date as date, count(*) as cnt from log_entry group by 1 order by 1")
        .columnMapping("date", "date").columnMapping("cnt", "value").create();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<LogEntry> findBy(LogEntryCriteria criteria) {
    // TODO: config cache duration
    return log(() -> cache.getOrElse(criteria.getCacheKey(), () -> LogEntry.findBy(criteria), 60),
        LOGGER, "findBy");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preSave(LogEntry t, boolean update) {
    if (t.user == null) {
      t.user = User.loggedInUser();
    }
  }

  @Override
  protected void prePersist(LogEntry t, boolean update) {
    if (t.project == null) {
      UUID projectId = ContextKey.ProjectId.get();
      if (projectId != null) {
        t.project = new Project().withId(projectId);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(Collection<LogEntry> t) {
    t.stream().forEach(e -> {
      preSave(e, false);
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(LogEntry t, boolean update) {
    if (t.user != null && t.project != null) {
      CompletableFuture.runAsync(() -> {
        try {
          notificationService.publish(t.user, t.project, t);
        } catch (IOException | StreamClientException e) {
          LOGGER.error("Error while publishing notification", e);
        }
      }, executionContext.current());
    }
  }
}
