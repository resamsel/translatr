package services.impl;

import criterias.LocaleCriteria;
import models.Locale;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LocaleRepository;
import repositories.Persistence;
import services.AuthProvider;
import services.CacheService;
import services.LocaleService;
import services.LogEntryService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

  @Inject
  public LocaleServiceImpl(Validator validator, CacheService cache,
                           LocaleRepository localeRepository, LogEntryService logEntryService,
                           Persistence persistence, AuthProvider authProvider) {
    super(validator, cache, localeRepository, Locale::getCacheKey, logEntryService, authProvider);

    this.localeRepository = localeRepository;
    this.persistence = persistence;
  }

  @Override
  public List<Locale> latest(Project project, int limit) {
    return cache.getOrElse(
        String.format("project:id:%s:latest:locales:%d", project.id, limit),
        () -> localeRepository.latest(project, limit),
        60);
  }

  @Override
  public Locale byProjectAndName(Project project, String name) {
    return cache.getOrElse(
        String.format("project:id:%s:locale:%s", project.id, name),
        () -> localeRepository.byProjectAndName(project, name),
        60);
  }

  @Override
  public Locale byOwnerAndProjectAndName(String username, String projectName, String localeName,
                                         String... fetches) {
    return cache.getOrElse(
        String.format("locale:owner:%s:projectName:%s:name:%s", username, projectName, localeName),
        () -> localeRepository.byOwnerAndProjectAndName(username, projectName, localeName, fetches),
        60
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<UUID, Double> progress(UUID projectId) {
    return localeRepository.progress(projectId);
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
  protected void postCreate(Locale t) {
    super.postCreate(t);

    // When locale has been created, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.project.id));
    if (t.project.owner != null) {
      cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));
      cache.removeByPrefix("locale:criteria:null:" + t.project.owner.username + ":" + t.project.name);
    }

    cache.removeByPrefix("locale:criteria:" + t.project.id);
  }

  @Override
  protected void postUpdate(Locale t) {
    super.postUpdate(t);

    // When locale has been updated, the locale cache needs to be invalidated
    cache.removeByPrefix("locale:criteria:" + t.project.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Locale t) {
    super.postDelete(t);

    // When locale has been deleted, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.project.id));
    cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));

    // When locale has been deleted, the locale cache needs to be invalidated
    cache.removeByPrefix("locale:criteria:" + t.project.id);
  }
}
