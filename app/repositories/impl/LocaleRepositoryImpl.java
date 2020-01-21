package repositories.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol.Activities;
import actors.ActivityProtocol.Activity;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSqlBuilder;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import criterias.PagedListFactory;
import dto.PermissionException;
import mappers.LocaleMapper;
import models.ActionType;
import models.Locale;
import models.Message;
import models.Project;
import models.ProjectRole;
import models.Stat;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LocaleRepository;
import repositories.MessageRepository;
import repositories.Persistence;
import services.AuthProvider;
import services.PermissionService;
import utils.QueryUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.Stopwatch.log;

@Singleton
public class LocaleRepositoryImpl extends
    AbstractModelRepository<Locale, UUID, LocaleCriteria> implements LocaleRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleRepositoryImpl.class);
  private static final String PROGRESS_COLUMN_ID = "l.id";
  private static final String PROGRESS_COLUMN_COUNT = "cast(count(distinct m.id) as decimal)/cast(count(distinct k.id) as decimal)";

  private final Find<UUID, Locale> find = new Find<UUID, Locale>() {
  };
  private final MessageRepository messageRepository;
  private final PermissionService permissionService;

  @Inject
  public LocaleRepositoryImpl(Persistence persistence,
                              Validator validator,
                              AuthProvider authProvider,
                              ActivityActorRef activityActor,
                              MessageRepository messageRepository,
                              PermissionService permissionService) {
    super(persistence, validator, authProvider, activityActor);

    this.messageRepository = messageRepository;
    this.permissionService = permissionService;
  }

  @Override
  public PagedList<Locale> findBy(LocaleCriteria criteria) {
    Query<Locale> q = fetch();

    if (StringUtils.isEmpty(criteria.getMessagesKeyName()) && !criteria.getFetches().isEmpty()) {
      QueryUtils.fetch(q, QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, criteria.getFetches()),
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

    if (Boolean.TRUE.equals(criteria.getMissing())) {
      ExpressionList<Message> messageQuery =
          Ebean.createQuery(Message.class).where().raw("locale.id = l.id");

      if (criteria.getKeyId() != null) {
        messageQuery.eq("key.id", criteria.getKeyId());
      }

      query.notExists(messageQuery.query());
    }

    if (criteria.getOrder() != null) {
      query.setOrderBy(criteria.getOrder());
    }

    criteria.paged(query);

    return fetch(
        log(() -> PagedListFactory.create(query, criteria.hasFetch(FETCH_COUNT)), LOGGER, "findBy"),
        criteria
    );
  }

  @Override
  public Locale byId(UUID id, String... fetches) {
    return QueryUtils.fetch(find.setId(id).setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP).findUnique();
  }

  private Query<Locale> fetch(String... fetches) {
    return QueryUtils.fetch(find.query().alias("l").setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP);
  }

  private PagedList<Locale> fetch(@Nonnull PagedList<Locale> paged, @Nonnull LocaleCriteria criteria) {
    if (StringUtils.isNotEmpty(criteria.getMessagesKeyName())
        && criteria.hasFetch("messages")) {
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

    if (criteria.hasFetch(FETCH_PROGRESS)) {
      Map<UUID, Double> progressMap = progress(criteria.getProjectId());

      paged.getList()
          .forEach(l -> l.progress = progressMap.getOrDefault(l.id, 0.0));
    }

    return paged;
  }

  @Override
  public Map<UUID, Double> progress(UUID projectId) {
    List<Stat> stats = log(
        () -> Ebean.find(Stat.class)
            .setRawSql(RawSqlBuilder
                .parse("SELECT " +
                    PROGRESS_COLUMN_ID + ", " + PROGRESS_COLUMN_COUNT +
                    " FROM locale l" +
                    " LEFT OUTER JOIN message m ON m.locale_id = l.id" +
                    " LEFT OUTER JOIN key k ON k.project_id = l.project_id" +
                    " GROUP BY " + PROGRESS_COLUMN_ID)
                .columnMapping(PROGRESS_COLUMN_ID, "id")
                .columnMapping(PROGRESS_COLUMN_COUNT, "count")
                .create())
            .where()
            .eq("l.project_id", projectId)
            .findList(),
        LOGGER,
        "Retrieving locale progress"
    );

    return stats.stream().collect(Collectors.toMap(stat -> stat.id, stat -> stat.count));
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

  @Override
  public Locale byOwnerAndProjectAndName(String username, String projectName, String localeName,
                                         String... fetches) {
    return fetch(fetches)
        .where()
        .eq("project.owner.username", username)
        .eq("project.name", projectName)
        .eq("name", localeName)
        .findUnique();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void prePersist(Locale t, boolean update) {
    if (update) {
      activityActor.tell(
          new Activity<>(ActionType.Update, authProvider.loggedInUser(), t.project, dto.Locale.class,
              LocaleMapper.toDto(byId(t.id)), LocaleMapper.toDto(t)),
          null
      );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Locale t, boolean update) {
    if (!update) {
      activityActor.tell(
          new Activity<>(ActionType.Create, authProvider.loggedInUser(), t.project, dto.Locale.class, null, LocaleMapper.toDto(t)),
          null
      );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(Locale t) {
    if (!permissionService
        .hasPermissionAny(t.project.id, authProvider.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager,
            ProjectRole.Translator)) {
      throw new PermissionException("User not allowed in project");
    }

    activityActor.tell(
        new Activity<>(ActionType.Delete, authProvider.loggedInUser(), t.project, dto.Locale.class, LocaleMapper.toDto(t), null),
        null
    );

    messageRepository.delete(messageRepository.byLocale(t.id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(Collection<Locale> t) {
    activityActor.tell(
        new Activities<>(t.stream().map(l -> new Activity<>(ActionType.Delete, authProvider.loggedInUser(), l.project,
            dto.Locale.class, LocaleMapper.toDto(l), null)).collect(Collectors.toList())),
        null
    );

    messageRepository.delete(
        messageRepository.byLocales(t.stream().map(m -> m.id).collect(Collectors.toList())));
  }
}
