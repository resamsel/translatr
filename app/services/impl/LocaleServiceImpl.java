package services.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol;
import criterias.GetCriteria;
import dto.PermissionException;
import io.ebean.PagedList;
import criterias.LocaleCriteria;
import mappers.LocaleMapper;
import models.ActionType;
import models.Locale;
import models.Project;
import models.ProjectRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.LocaleRepository;
import repositories.MessageRepository;
import repositories.Persistence;
import services.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static utils.Stopwatch.log;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class LocaleServiceImpl extends AbstractModelService<Locale, UUID, LocaleCriteria>
        implements LocaleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceImpl.class);

  private final LocaleRepository localeRepository;
  private final Persistence persistence;
  private final MetricService metricService;
  private final LocaleMapper localeMapper;
  private final PermissionService permissionService;
  private final MessageRepository messageRepository;

  @Inject
  public LocaleServiceImpl(Validator validator, CacheService cache,
                           LocaleRepository localeRepository, LogEntryService logEntryService,
                           Persistence persistence, AuthProvider authProvider, MetricService metricService,
                           ActivityActorRef activityActor, LocaleMapper localeMapper, PermissionService permissionService,
                           MessageRepository messageRepository) {
    super(validator, cache, localeRepository, Locale::getCacheKey, logEntryService, authProvider, activityActor);

    this.localeRepository = localeRepository;
    this.persistence = persistence;
    this.metricService = metricService;
    this.localeMapper = localeMapper;
    this.permissionService = permissionService;
    this.messageRepository = messageRepository;
  }

  @Override
  public Locale byProjectAndName(Project project, String name, Http.Request request) {
    return postGet(cache.getOrElseUpdate(
            String.format("project:id:%s:locale:%s", project.id, name),
            () -> localeRepository.byProjectAndName(project, name),
            60), request);
  }

  @Override
  public Locale byOwnerAndProjectAndName(String username, String projectName, String localeName, Http.Request request,
                                         String... fetches) {
    return postGet(cache.getOrElseUpdate(
            String.format("locale:owner:%s:projectName:%s:name:%s", username, projectName, localeName),
            () -> localeRepository.byOwnerAndProjectAndName(username, projectName, localeName, fetches),
            60
    ), request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void increaseWordCountBy(UUID localeId, int wordCountDiff, Http.Request request) {
    if (wordCountDiff == 0) {
      LOGGER.debug("Not changing word count");
      return;
    }

    Locale locale = modelRepository.byId(GetCriteria.from(localeId, request));

    if (locale == null) {
      return;
    }

    if (locale.wordCount == null) {
      locale.wordCount = 0;
    }
    locale.wordCount += wordCountDiff;

    log(() -> modelRepository.persist(locale), LOGGER, "Increased word count by %d", wordCountDiff);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    try {
      persistence.createSqlUpdate("update locale set word_count = null where project_id = :projectId")
              .setParameter("projectId", projectId).execute();
    } catch (Exception e) {
      LOGGER.error("Error while resetting word count", e);
    }
  }

  @Override
  protected PagedList<Locale> postFind(PagedList<Locale> pagedList, Http.Request request) {
    metricService.logEvent(Locale.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  protected List<Locale> postFind(List<Locale> list) {
    metricService.logEvent(Locale.class, ActionType.Read);

    return list;
  }

  @Override
  protected Locale postGet(Locale locale, Http.Request request) {
    metricService.logEvent(Locale.class, ActionType.Read);

    return super.postGet(locale, request);
  }

  @Override
  protected void postCreate(Locale t, Http.Request request) {
    super.postCreate(t, request);

    metricService.logEvent(Locale.class, ActionType.Create);

    // When locale has been created, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.project.id));
    if (t.project.owner != null) {
      cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));
      cache.removeByPrefix("locale:criteria:null:" + t.project.owner.username + ":" + t.project.name);
    }

    cache.removeByPrefix("locale:criteria:" + t.project.id);

    activityActor.tell(
            new ActivityProtocol.Activity<>(ActionType.Create, authProvider.loggedInUser(request), t.project, dto.Locale.class, null, localeMapper.toDto(t, request)),
            null
    );
  }

  @Override
  protected void preUpdate(Locale t, Http.Request request) {
    super.preUpdate(t, request);

    activityActor.tell(
            new ActivityProtocol.Activity<>(ActionType.Update, authProvider.loggedInUser(request), t.project, dto.Locale.class,
                    localeMapper.toDto(byId(GetCriteria.from(t.id, request)), request), localeMapper.toDto(t, request)),
            null
    );
  }

  @Override
  protected Locale postUpdate(Locale t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(Locale.class, ActionType.Update);

    // When locale has been updated, the locale cache needs to be invalidated
    cache.removeByPrefix("locale:criteria:" + t.project.id);

    return t;
  }

  @Override
  public void preDelete(Locale t, Http.Request request) {
    super.preDelete(t, request);

    if (!permissionService
            .hasPermissionAny(t.project.id, authProvider.loggedInUser(request), ProjectRole.Owner, ProjectRole.Manager,
                    ProjectRole.Translator)) {
      throw new PermissionException("User not allowed in project");
    }

    activityActor.tell(
            new ActivityProtocol.Activity<>(ActionType.Delete, authProvider.loggedInUser(request), t.project, dto.Locale.class, localeMapper.toDto(t, request), null),
            null
    );

    messageRepository.delete(messageRepository.byLocale(t.id));
  }

  @Override
  protected void postDelete(Locale t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(Locale.class, ActionType.Delete);

    // When locale has been deleted, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.project.id));
    cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));

    // When locale has been deleted, the locale cache needs to be invalidated
    cache.removeByPrefix("locale:criteria:" + t.project.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preDelete(Collection<Locale> t, Http.Request request) {
    super.preDelete(t, request);

    activityActor.tell(
            new ActivityProtocol.Activities<>(t.stream().map(l -> new ActivityProtocol.Activity<>(ActionType.Delete, authProvider.loggedInUser(request), l.project,
                    dto.Locale.class, localeMapper.toDto(l, null), null)).collect(Collectors.toList())),
            null
    );

    messageRepository.delete(
            messageRepository.byLocales(t.stream().map(m -> m.id).collect(Collectors.toList())));
  }
}
