package repositories.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.Stopwatch.log;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import criterias.HasNextPagedList;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import dto.PermissionException;
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
import models.Locale;
import models.LogEntry;
import models.Message;
import models.Project;
import models.ProjectRole;
import models.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import repositories.LocaleRepository;
import repositories.LogEntryRepository;
import repositories.MessageRepository;
import services.PermissionService;
import utils.QueryUtils;

@Singleton
public class LocaleRepositoryImpl extends
    AbstractModelRepository<Locale, UUID, LocaleCriteria> implements LocaleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleRepositoryImpl.class);

  private final Find<UUID, Locale> find = new Find<UUID, Locale>() {
  };
  private final MessageRepository messageRepository;
  private final PermissionService permissionService;

  @Inject
  public LocaleRepositoryImpl(Validator validator, CacheApi cache,
      MessageRepository messageRepository, LogEntryRepository logEntryRepository,
      PermissionService permissionService) {
    super(validator, cache, logEntryRepository);

    this.messageRepository = messageRepository;
    this.permissionService = permissionService;
  }

  @Override
  public PagedList<Locale> findBy(LocaleCriteria criteria) {
    Query<Locale> q = fetch();

    if (StringUtils.isEmpty(criteria.getMessagesKeyName()) && !criteria.getFetches().isEmpty()) {
      q = QueryUtils.fetch(q, QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, criteria.getFetches()),
          FETCH_MAP);
    }

    ExpressionList<Locale> query = q.where();

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getLocaleName() != null) {
      query.eq("name", criteria.getLocaleName());
    }

    if (criteria.getSearch() != null) {
      query.ilike("name", "%" + criteria.getSearch() + "%");
    }

    if (criteria.getOrder() != null) {
      query.setOrderBy(criteria.getOrder());
    }

    criteria.paged(query);

    return log(() -> fetch(HasNextPagedList.create(query), criteria), LOGGER, "findBy");
  }

  @Override
  public Locale byId(UUID id, String... fetches) {
    return QueryUtils.fetch(find.setId(id).setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP).findUnique();
  }

  private Query<Locale> fetch(String... fetches) {
    return QueryUtils.fetch(find.query().setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP);
  }

  private HasNextPagedList<Locale> fetch(HasNextPagedList<Locale> paged,
      LocaleCriteria criteria) {
    if (StringUtils.isNotEmpty(criteria.getMessagesKeyName())
        && criteria.getFetches().contains("messages")) {
      // Retrieve messages that match the given keyName and locales retrieved
      Map<UUID, Message> messages = messageRepository
          .findBy(new MessageCriteria().withKeyName(criteria.getMessagesKeyName())
              .withLocaleIds(paged.getList().stream().map(l -> l.id).collect(toList())))
          .getList().stream().collect(toMap(m -> m.locale.id, m -> m));

      for (Locale locale : paged.getList()) {
        if (messages.containsKey(locale.id)) {
          locale.messages = Collections.singletonList(messages.get(locale.id));
        }
      }
    }

    return paged;
  }

  @Override
  public List<Locale> latest(Project project, int limit) {
    return log(
        () -> fetch().where().eq("project", project).order("whenUpdated desc").setMaxRows(limit)
            .findList(), LOGGER, "last(%d)", limit);
  }

  @Override
  public Locale byProjectAndName(Project project, String name) {
    if (project == null) {
      return null;
    }

    return byProjectAndName(project.id, name);
  }

  public Locale byProjectAndName(UUID projectId, String name) {
    return fetch().where().eq("project.id", projectId).eq("name", name).findUnique();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void prePersist(Locale t, boolean update) {
    if (update) {
      logEntryRepository.save(LogEntry.from(ActionType.Update, t.project, dto.Locale.class,
          dto.Locale.from(byId(t.id)), dto.Locale.from(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Locale t, boolean update) {
    if (!update) {
      logEntryRepository.save(
          LogEntry.from(ActionType.Create, t.project, dto.Locale.class, null, dto.Locale.from(t)));

      // When message has been created, the project cache needs to be invalidated
      cache.remove(Project.getCacheKey(t.project.id));
    }
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
  public void preDelete(Locale t) {
    if (!permissionService
        .hasPermissionAny(t.project.id, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager,
            ProjectRole.Translator)) {
      throw new PermissionException("User not allowed in project");
    }

    logEntryRepository.save(
        LogEntry.from(ActionType.Delete, t.project, dto.Locale.class, dto.Locale.from(t), null));

    messageRepository.delete(messageRepository.byLocale(t.id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(Collection<Locale> t) {
    logEntryRepository.save(t.stream().map(l -> LogEntry.from(ActionType.Delete, l.project,
        dto.Locale.class, dto.Locale.from(l), null)).collect(Collectors.toList()));

    messageRepository.delete(
        messageRepository.byLocales(t.stream().map(m -> m.id).collect(Collectors.toList())));
  }
}
