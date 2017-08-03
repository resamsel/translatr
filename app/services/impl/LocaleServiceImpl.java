package services.impl;

import static utils.Stopwatch.log;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSqlBuilder;
import criterias.LocaleCriteria;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.Locale;
import models.Project;
import models.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import repositories.LocaleRepository;
import repositories.MessageRepository;
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class LocaleServiceImpl extends AbstractModelService<Locale, UUID, LocaleCriteria>
    implements LocaleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceImpl.class);

  private final LocaleRepository localeRepository;

  @Inject
  public LocaleServiceImpl(Validator validator, CacheApi cache, LocaleRepository localeRepository,
      LogEntryService logEntryService) {
    super(validator, cache, localeRepository, Locale::getCacheKey, logEntryService);

    this.localeRepository = localeRepository;
  }

  @Override
  public List<Locale> latest(Project project, int limit) {
    return cache.getOrElse(
        String.format("project:%s:locales:latest:%d", project.id, limit),
        () -> localeRepository.latest(project, limit),
        60);
  }

  @Override
  public Locale byProjectAndName(Project project, String name) {
    return cache.getOrElse(
        String.format("project:%s:locale:%s", project.id, name),
        () -> localeRepository.byProjectAndName(project, name),
        60);
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
  public void increaseWordCountBy(UUID localeId, int wordCountDiff) {
    if (wordCountDiff == 0) {
      LOGGER.debug("Not changing word count");
      return;
    }

    Locale locale = modelRepository.byId(localeId);

    if (locale == null) {
      return;
    }

    if (locale.wordCount == null) {
      locale.wordCount = 0;
    }
    locale.wordCount += wordCountDiff;

    log(() -> persist(locale), LOGGER, "Increased word count by %d", wordCountDiff);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    try {
      Ebean.createSqlUpdate("update locale set word_count = null where project_id = :projectId")
          .setParameter("projectId", projectId).execute();
    } catch (Exception e) {
      LOGGER.error("Error while resetting word count", e);
    }
  }
}
