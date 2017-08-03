package repositories.impl;

import static utils.Stopwatch.log;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.google.common.collect.ImmutableMap;
import criterias.HasNextPagedList;
import criterias.KeyCriteria;
import dto.PermissionException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.ActionType;
import models.Key;
import models.LogEntry;
import models.Message;
import models.Project;
import models.ProjectRole;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import repositories.KeyRepository;
import repositories.LogEntryRepository;
import repositories.MessageRepository;
import services.PermissionService;
import utils.QueryUtils;

@Singleton
public class KeyRepositoryImpl extends AbstractModelRepository<Key, UUID, KeyCriteria> implements
    KeyRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyRepositoryImpl.class);

  private static final Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of("project", Arrays.asList("project", "project.owner"), "messages",
          Arrays.asList("messages", "messages.locale"));

  private static final List<String> PROPERTIES_TO_FETCH = Collections.singletonList("project");

  private final Find<UUID, Key> find = new Find<UUID, Key>() {
  };

  private final MessageRepository messageRepository;
  private final PermissionService permissionService;

  @Inject
  public KeyRepositoryImpl(Validator validator, CacheApi cache, MessageRepository messageRepository,
      LogEntryRepository logEntryRepository, PermissionService permissionService) {
    super(validator, cache, logEntryRepository);

    this.messageRepository = messageRepository;
    this.permissionService = permissionService;
  }

  @Override
  public PagedList<Key> findBy(KeyCriteria criteria) {
    Query<Key> q = fetch(criteria.getFetches());

    ExpressionList<Key> query = q.where();

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getNames() != null && !criteria.getNames().isEmpty()) {
      query.in("name", criteria.getNames());
    }

    if (criteria.getSearch() != null) {
      query.disjunction().ilike("name", "%" + criteria.getSearch() + "%")
          .exists(Ebean.createQuery(Message.class).where().raw("key.id = k.id")
              .ilike("value", "%" + criteria.getSearch() + "%").query())
          .endJunction();
    }

    if (criteria.getProjectOwnerUsername() != null) {
      query.eq("project.owner.username", criteria.getProjectOwnerUsername());
    }

    if (criteria.getProjectName() != null) {
      query.eq("project.name", criteria.getProjectName());
    }

    if (Boolean.TRUE.equals(criteria.getMissing())) {
      ExpressionList<Message> messageQuery =
          Ebean.createQuery(Message.class).where().raw("key.id = k.id");

      if (criteria.getLocaleId() != null) {
        messageQuery.eq("locale.id", criteria.getLocaleId());
      }

      query.notExists(messageQuery.query());
    }

    if (criteria.getOrder() != null) {
      query.setOrderBy(criteria.getOrder());
    }

    criteria.paged(query);

    return log(() -> HasNextPagedList.create(query), LOGGER, "findBy");
  }

  @Override
  public Key byId(UUID id, String... fetches) {
    return QueryUtils.fetch(find.setId(id).setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP).findUnique();
  }

  @Override
  public List<Key> latest(Project project, int limit) {
    return fetch().where().eq("project", project).order("whenUpdated desc")
        .setMaxRows(limit).findList();
  }

  @Override
  public Key byProjectAndName(Project project, String name) {
    if (project == null) {
      return null;
    }

    return byProjectAndName(project.id, name);
  }

  public Key byProjectAndName(UUID projectId, String name) {
    return fetch().where().eq("project.id", projectId).eq("name", name).findUnique();
  }

  private Query<Key> fetch(List<String> fetches) {
    return fetch(fetches.toArray(new String[fetches.size()]));
  }

  private Query<Key> fetch(String... fetches) {
    return QueryUtils.fetch(find.query().alias("k").setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void prePersist(Key t, boolean update) {
    if (update) {
      logEntryRepository.save(LogEntry.from(ActionType.Update, t.project, dto.Key.class,
          dto.Key.from(byId(t.id)), dto.Key.from(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Key t, boolean update) {
    if (!update) {
      logEntryRepository
          .save(LogEntry.from(ActionType.Create, t.project, dto.Key.class, null, dto.Key.from(t)));

      cache.remove(Project.getCacheKey(t.project.id));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preDelete(Key t) {
    if (!permissionService.hasPermissionAny(t.project.id, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager,
        ProjectRole.Developer)) {
      throw new PermissionException("User not allowed in project");
    }

    logEntryRepository
        .save(LogEntry.from(ActionType.Delete, t.project, dto.Key.class, dto.Key.from(t), null));

    messageRepository.delete(messageRepository.byKey(t));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Key t) {
    // When message has been created, the project cache needs to be invalidated
    cache.remove(Project.getCacheKey(t.project.id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preDelete(Collection<Key> t) {
    logEntryRepository.save(t.stream()
        .map(k -> LogEntry.from(ActionType.Delete, k.project, dto.Key.class, dto.Key.from(k), null))
        .collect(Collectors.toList()));

    messageRepository
        .delete(messageRepository.byKeys(t.stream().map(k -> k.id).collect(Collectors.toList())));
  }
}
