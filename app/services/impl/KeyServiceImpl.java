package services.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol;
import criterias.GetCriteria;
import dto.PermissionException;
import io.ebean.PagedList;
import criterias.KeyCriteria;
import mappers.KeyMapper;
import models.ActionType;
import models.Key;
import models.Project;
import models.ProjectRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.KeyRepository;
import repositories.MessageRepository;
import repositories.Persistence;
import services.*;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
  private final KeyMapper keyMapper;
  private final PermissionService permissionService;
  private final MessageRepository messageRepository;

  @Inject
  public KeyServiceImpl(Validator validator, CacheService cache, KeyRepository keyRepository,
                        LogEntryService logEntryService, Persistence persistence, AuthProvider authProvider,
                        MetricService metricService, ActivityActorRef activityActor, KeyMapper keyMapper,
                        PermissionService permissionService, MessageRepository messageRepository) {
    super(validator, cache, keyRepository, Key::getCacheKey, logEntryService, authProvider, activityActor);

    this.keyRepository = keyRepository;
    this.persistence = persistence;
    this.metricService = metricService;
    this.keyMapper = keyMapper;
    this.permissionService = permissionService;
    this.messageRepository = messageRepository;
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
  protected void preUpdate(Key t, Http.Request request) {
    super.preUpdate(t, request);

    activityActor.tell(
            new ActivityProtocol.Activity<>(
                    ActionType.Update, authProvider.loggedInUser(request), t.project, dto.Key.class,
                    keyMapper.toDto(byId(GetCriteria.from(t.id, request)), request), keyMapper.toDto(t, request)),
            null
    );
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

    activityActor.tell(
            new ActivityProtocol.Activity<>(ActionType.Create, authProvider.loggedInUser(request), t.project, dto.Key.class, null, keyMapper.toDto(t, request)),
            null
    );
  }

  @Override
  protected Key postUpdate(Key t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(Key.class, ActionType.Update);

    Optional<Key> existing = cache.get(Key.getCacheKey(t.id));
    if (existing.isPresent()) {
      cache.removeByPrefix(getCacheKey(existing.get().project.id, existing.get().name));
    } else {
      cache.removeByPrefix(getCacheKey(t.project.id, ""));
    }

    // When locale has been updated, the locale cache needs to be invalidated
    cache.removeByPrefix("key:criteria:" + t.project.id);

    return t;
  }

  @Override
  protected void preDelete(Key t, Http.Request request) {
    super.preDelete(t, request);

    if (!permissionService
            .hasPermissionAny(t.project.id, authProvider.loggedInUser(request), ProjectRole.Owner, ProjectRole.Manager,
                    ProjectRole.Developer)) {
      throw new PermissionException("User not allowed in project");
    }

    activityActor.tell(
            new ActivityProtocol.Activity<>(ActionType.Delete, authProvider.loggedInUser(request), t.project, dto.Key.class, keyMapper.toDto(t, request), null),
            null
    );

    messageRepository.delete(messageRepository.byKey(t));
  }

  @Override
  protected void postDelete(Key t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(Key.class, ActionType.Delete);

    Key existing = byId(GetCriteria.from(t.id, request));
    if (existing != null) {
      cache.removeByPrefix(getCacheKey(existing.project.id, existing.name));
    }

    // When key has been deleted, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.project.id));
    cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));

    // When key has been deleted, the key cache needs to be invalidated
    cache.removeByPrefix("key:criteria:" + t.project.id);
  }

  @Override
  protected void preDelete(Collection<Key> t, Http.Request request) {
    super.preDelete(t, request);

    activityActor.tell(
            new ActivityProtocol.Activities<>(
                    t.stream()
                            .map(k -> new ActivityProtocol.Activity<>(ActionType.Delete, authProvider.loggedInUser(request), k.project, dto.Key.class,
                                    keyMapper.toDto(k, request), null))
                            .collect(Collectors.toList())),
            null
    );

    messageRepository
            .delete(messageRepository.byKeys(t.stream().map(k -> k.id).collect(Collectors.toList())));
  }

  private static String getCacheKey(UUID projectId, String keyName) {
    return String.format("key:project:%s:name:%s", projectId, keyName);
  }
}
