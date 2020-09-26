package services.impl;

import criterias.GetCriteria;
import io.ebean.PagedList;
import criterias.KeyCriteria;
import models.ActionType;
import models.Key;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.KeyRepository;
import repositories.Persistence;
import services.*;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static utils.Stopwatch.log;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
public class KeyServiceImpl extends AbstractModelService<Key, UUID, KeyCriteria>
    implements KeyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceImpl.class);

  private final KeyRepository keyRepository;
  private final Persistence persistence;
  private final MetricService metricService;

  private final CacheService cache;

  @Inject
  public KeyServiceImpl(Validator validator, CacheService cache, KeyRepository keyRepository,
                        LogEntryService logEntryService, Persistence persistence, AuthProvider authProvider,
                        MetricService metricService) {
    super(validator, cache, keyRepository, Key::getCacheKey, logEntryService, authProvider);

    this.cache = cache;
    this.keyRepository = keyRepository;
    this.persistence = persistence;
    this.metricService = metricService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void increaseWordCountBy(UUID keyId, int wordCountDiff, Http.Request request) {
    if (wordCountDiff == 0) {
      LOGGER.debug("Not changing word count");
      return;
    }

    Key key = modelRepository.byId(GetCriteria.from(keyId, request));

    if (key == null) {
      return;
    }

    if (key.wordCount == null) {
      key.wordCount = 0;
    }
    key.wordCount += wordCountDiff;

    log(
        () -> modelRepository.persist(key),
        LOGGER,
        "Increased word count by %d",
        wordCountDiff
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    try {
      persistence.createSqlUpdate("update key set word_count = null where project_id = :projectId")
          .setParameter("projectId", projectId).execute();
    } catch (Exception e) {
      LOGGER.error("Error while resetting word count", e);
    }
  }

  @Override
  public Key byProjectAndName(Project project, String name, Http.Request request) {
    return postGet(cache.getOrElseUpdate(
            getCacheKey(project.id, name),
            () -> keyRepository.byProjectAndName(project, name),
            60
    ), request);
  }

  @Override
  public Key byOwnerAndProjectAndName(String username, String projectName, String keyName, Http.Request request, String... fetches) {
    return postGet(cache.getOrElseUpdate(
            String.format("key:owner:%s:projectName:%s:name:%s", username, projectName, keyName),
            () -> keyRepository.byOwnerAndProjectAndName(username, projectName, keyName, fetches),
            60
    ), request);
  }

  @Override
  protected PagedList<Key> postFind(PagedList<Key> pagedList, Http.Request request) {
    metricService.logEvent(Key.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  protected List<Key> postFind(List<Key> list) {
    metricService.logEvent(Key.class, ActionType.Read);

    return list;
  }

  @Override
  protected Key postGet(Key key, Http.Request request) {
    metricService.logEvent(Key.class, ActionType.Read);

    return super.postGet(key, request);
  }

  @Override
  protected void postCreate(Key t, Http.Request request) {
    super.postCreate(t, request);

    metricService.logEvent(Key.class, ActionType.Create);

    // When key has been created, the project cache needs to be invalidated
    cache.remove(Project.getCacheKey(t.project.id));
    if (t.project.owner != null) {
      cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));
      cache.removeByPrefix("key:criteria:null:" + t.project.owner.username + ":" + t.project.name);
    }

    cache.removeByPrefix("key:criteria:" + t.project.id);
  }

  @Override
  protected Key postUpdate(Key t, Http.Request request) {
    metricService.logEvent(Key.class, ActionType.Update);

    Optional<Key> existing = cache.get(Key.getCacheKey(t.id));
    if (existing.isPresent()) {
      cache.removeByPrefix(getCacheKey(existing.get().project.id, existing.get().name));
    } else {
      cache.removeByPrefix(getCacheKey(t.project.id, ""));
    }

    super.postUpdate(t, request);

    // When locale has been updated, the locale cache needs to be invalidated
    cache.removeByPrefix("key:criteria:" + t.project.id);

    return t;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Key t, Http.Request request) {
    metricService.logEvent(Key.class, ActionType.Delete);

    Key existing = byId(t.id, request);
    if (existing != null) {
      cache.removeByPrefix(getCacheKey(existing.project.id, existing.name));
    }

    super.postDelete(t, request);

    // When key has been deleted, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.project.id));
    cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));

    // When key has been deleted, the key cache needs to be invalidated
    cache.removeByPrefix("key:criteria:" + t.project.id);
  }

  private static String getCacheKey(UUID projectId, String keyName) {
    return String.format("key:project:%s:name:%s", projectId, keyName);
  }
}
