package services.impl;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import criterias.LogEntryCriteria;
import criterias.PagedListFactory;
import models.Aggregate;
import models.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LogEntryRepository;
import repositories.Persistence;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Optional;
import java.util.UUID;

import static repositories.impl.AbstractModelRepository.FETCH_COUNT;
import static utils.Stopwatch.log;

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
  private final LogEntryRepository logEntryRepository;
  private final Persistence persistence;

  @Inject
  public LogEntryServiceImpl(Validator validator, CacheService cache,
                             LogEntryRepository logEntryRepository, Persistence persistence, AuthProvider authProvider) {
    super(validator, cache, logEntryRepository, LogEntry::getCacheKey, null, authProvider);

    this.logEntryRepository = logEntryRepository;
    this.persistence = persistence;
  }

  @Override
  public int countBy(LogEntryCriteria criteria) {
    return cache.getOrElse(
        criteria.getCacheKey(),
        () -> logEntryRepository.countBy(criteria),
        60
    );
  }

  @Override
  public PagedList<Aggregate> getAggregates(LogEntryCriteria criteria) {
    ExpressionList<Aggregate> query =
        persistence.createQuery(Aggregate.class)
            .setRawSql(getAggregatesRawSql())
            .setMaxRows(Optional.ofNullable(criteria.getLimit()).orElse(1000))
            .setFirstRow(Optional.ofNullable(criteria.getOffset()).orElse(0))
            .where();

    if (criteria.getProjectId() != null) {
      query.eq("project_id", criteria.getProjectId());
    }

    if (criteria.getUserId() != null) {
      query.eq("user_id", criteria.getUserId());
    }

    String cacheKey =
        String.format("activity:aggregates:%s:%s", criteria.getUserId(), criteria.getProjectId());

    return log(
        () -> cache.getOrElse(
                cacheKey,
                () -> PagedListFactory.create(query, criteria.hasFetch(FETCH_COUNT)),
                60
        ),
        LOGGER,
        "Retrieving log entry aggregates"
    );
  }

  private RawSql getAggregatesRawSql() {
    String dbpName = persistence.getDatabasePlatformName();
    if ("h2".equals(dbpName)) {
      return RawSqlBuilder
          .parse(String.format(
              "select %1$s as millis, count(*) as cnt from log_entry group by %1$s order by 1",
              H2_COLUMN_MILLIS))
          .columnMapping(H2_COLUMN_MILLIS, "millis")
          .columnMapping("count(*)", "value")
          .create();
    }

    return RawSqlBuilder
        .parse(
            "select when_created::date as date, count(*) as cnt from log_entry group by 1 order by 1")
        .columnMapping("date", "date")
        .columnMapping("cnt", "value")
        .create();
  }

  @Override
  protected void postCreate(LogEntry t) {
    super.postCreate(t);

    // When user has been created
    cache.removeByPrefix("activity:criteria:");
  }

  @Override
  protected LogEntry postUpdate(LogEntry t) {
    super.postUpdate(t);

    // When user has been updated, the user cache needs to be invalidated
    cache.removeByPrefix("activity:criteria:");

    return t;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(LogEntry t) {
    super.postDelete(t);

    // When locale has been deleted, the locale cache needs to be invalidated
    cache.removeByPrefix("activity:criteria:");
  }
}
