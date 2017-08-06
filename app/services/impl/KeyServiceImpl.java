package services.impl;

import static utils.Stopwatch.log;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSqlBuilder;
import criterias.KeyCriteria;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.Validator;
import models.Key;
import models.Project;
import models.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.KeyRepository;
import services.CacheService;
import services.KeyService;
import services.LogEntryService;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
public class KeyServiceImpl extends AbstractModelService<Key, UUID, KeyCriteria>
    implements KeyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceImpl.class);

  private final KeyRepository keyRepository;

  private final CacheService cache;

  @Inject
  public KeyServiceImpl(Validator validator, CacheService cache, KeyRepository keyRepository,
      LogEntryService logEntryService) {
    super(validator, cache, keyRepository, Key::getCacheKey, logEntryService);

    this.cache = cache;
    this.keyRepository = keyRepository;
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
  public void increaseWordCountBy(UUID keyId, int wordCountDiff) {
    if (wordCountDiff == 0) {
      LOGGER.debug("Not changing word count");
      return;
    }

    Key key = modelRepository.byId(keyId);

    if (key == null) {
      return;
    }

    if (key.wordCount == null) {
      key.wordCount = 0;
    }
    key.wordCount += wordCountDiff;

    log(() -> modelRepository.persist(key), LOGGER, "Increased word count by %d", wordCountDiff);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    try {
      Ebean.createSqlUpdate("update key set word_count = null where project_id = :projectId")
          .setParameter("projectId", projectId).execute();
    } catch (Exception e) {
      LOGGER.error("Error while resetting word count", e);
    }
  }

  @Override
  public List<Key> latest(Project project, int limit) {
    return cache.getOrElse(
        String.format("project:%s:keys:latest:%d", project.id, limit),
        () -> keyRepository.latest(project, limit),
        60);
  }

  @Override
  public Key byProjectAndName(Project project, String name) {
    return cache.getOrElse(
        String.format("project:%s:key:%s", project.id, name),
        () -> keyRepository.byProjectAndName(project, name),
        60);
  }

  @Override
  protected void postSave(Key t) {
    cache.remove(Project.getCacheKey(t.project.id));
  }
}
