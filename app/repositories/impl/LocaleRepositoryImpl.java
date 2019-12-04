package repositories.impl;

import actors.ActivityActor;
import actors.ActivityProtocol.Activities;
import actors.ActivityProtocol.Activity;
import akka.actor.ActorRef;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
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
import models.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LocaleRepository;
import repositories.MessageRepository;
import services.PermissionService;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Named;
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

  private final Find<UUID, Locale> find = new Find<UUID, Locale>() {
  };
  private final MessageRepository messageRepository;
  private final PermissionService permissionService;

  @Inject
  public LocaleRepositoryImpl(
      Validator validator, @Named(ActivityActor.NAME) ActorRef activityActor,
      MessageRepository messageRepository, PermissionService permissionService) {
    super(validator, activityActor);

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

    return log(
        () -> fetch(PagedListFactory.create(query, criteria.getFetches().contains(FETCH_COUNT)), criteria),
        LOGGER,
        "findBy"
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

  private PagedList<Locale> fetch(PagedList<Locale> paged, LocaleCriteria criteria) {
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
          new Activity<>(ActionType.Update, User.loggedInUser(), t.project, dto.Locale.class,
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
          new Activity<>(ActionType.Create, User.loggedInUser(), t.project, dto.Locale.class, null, LocaleMapper.toDto(t)),
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
        .hasPermissionAny(t.project.id, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager,
            ProjectRole.Translator)) {
      throw new PermissionException("User not allowed in project");
    }

    activityActor.tell(
        new Activity<>(ActionType.Delete, User.loggedInUser(), t.project, dto.Locale.class, LocaleMapper.toDto(t), null),
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
        new Activities<>(t.stream().map(l -> new Activity<>(ActionType.Delete, User.loggedInUser(), l.project,
            dto.Locale.class, LocaleMapper.toDto(l), null)).collect(Collectors.toList())),
        null
    );

    messageRepository.delete(
        messageRepository.byLocales(t.stream().map(m -> m.id).collect(Collectors.toList())));
  }
}
