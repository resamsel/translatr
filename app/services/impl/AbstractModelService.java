package services.impl;

import actors.ActivityActorRef;
import criterias.AbstractSearchCriteria;
import criterias.GetCriteria;
import io.ebean.PagedList;
import models.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import repositories.ModelRepository;
import services.AuthProvider;
import services.CacheService;
import services.LogEntryService;
import services.ModelService;

import javax.validation.Validator;
import java.util.Collection;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

/**
 * @author resamsel
 * @version 9 Sep 2016
 */
public abstract class AbstractModelService<MODEL extends Model<MODEL, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>>
    implements ModelService<MODEL, ID, CRITERIA> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelService.class);

  protected final Validator validator;
  protected final LogEntryService logEntryService;
  protected final AuthProvider authProvider;
  protected final ActivityActorRef activityActor;
  protected final CacheService cache;
  final ModelRepository<MODEL, ID, CRITERIA> modelRepository;
  private final BiFunction<ID, String[], String> cacheKeyGetter;

  public AbstractModelService(Validator validator, CacheService cache,
                              ModelRepository<MODEL, ID, CRITERIA> modelRepository,
                              BiFunction<ID, String[], String> cacheKeyGetter, LogEntryService logEntryService,
                              AuthProvider authProvider, ActivityActorRef activityActor) {
    this.validator = validator;
    this.cache = cache;
    this.modelRepository = modelRepository;
    this.cacheKeyGetter = cacheKeyGetter;
    this.logEntryService = logEntryService;
    this.authProvider = authProvider;
    this.activityActor = activityActor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<MODEL> findBy(CRITERIA criteria) {
    criteria.setLoggedInUserId(authProvider.loggedInUserId(criteria.getRequest()));

    return postFind(cache.getOrElseUpdate(
            requireNonNull(criteria, "criteria is null").getCacheKey(),
            () -> modelRepository.findBy(criteria),
            60
    ), criteria.getRequest());
  }

  protected PagedList<MODEL> postFind(PagedList<MODEL> pagedList, Http.Request request) {
    return pagedList;
  }

  @Override
  public MODEL byId(ID id, Http.Request request, String... fetches) {
    if (id == null) {
      return null;
    }

    return byId(GetCriteria.from(id, request, fetches));
  }

  @Override
  public MODEL byId(GetCriteria<ID> criteria) {
    if (criteria == null) {
      return null;
    }

    if (criteria.getLoggedInUserId() == null) {
      criteria.setLoggedInUserId(authProvider.loggedInUserId(criteria.getRequest()));
    }

    return postGet(cache.getOrElseUpdate(
            cacheKeyGetter.apply(criteria.getId(), criteria.getFetches().toArray(new String[0])),
            () -> modelRepository.byId(criteria),
            60
            ),
            criteria.getRequest());
  }

  protected MODEL postGet(MODEL model, Http.Request request) {
    return model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL create(MODEL model, Http.Request request) {
    LOGGER.debug("create({})", model);

    preCreate(model, request);
    preSave(model, request);

    MODEL m = modelRepository.create(model);

    postSave(m, request);
    postCreate(m, request);

    return m;
  }

  protected void preCreate(MODEL t, Http.Request request) {
  }

  protected void postCreate(MODEL t, Http.Request request) {
    cache.set(cacheKeyGetter.apply(t.getId(), new String[0]), t, 60);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL update(MODEL t, Http.Request request) {
    LOGGER.debug("update({})", t);

    preUpdate(t, request);
    preSave(t, request);

    MODEL m = modelRepository.update(t);

    postSave(m, request);
    postUpdate(m, request);

    return m;
  }

  protected void preUpdate(MODEL t, Http.Request request) {
  }

  protected void preSave(MODEL t, Http.Request request) {
  }

  protected void postSave(MODEL t, Http.Request request) {
  }

  protected MODEL postUpdate(MODEL t, Http.Request request) {
    cache.removeByPrefix(cacheKeyGetter.apply(t.getId(), new String[0]));
    cache.set(cacheKeyGetter.apply(t.getId(), new String[0]), t, 60);

    return t;
  }

  @Override
  public Collection<MODEL> save(Collection<MODEL> t, Http.Request request) {
    preSave(t, request);

    Collection<MODEL> m = modelRepository.save(t);

    postSave(m, request);

    return m;
  }

  protected void preSave(Collection<MODEL> t, Http.Request request) {
    t.forEach(e -> preSave(e, request));
  }

  protected void postSave(Collection<MODEL> t, Http.Request request) {
    t.forEach(e -> postSave(e, request));
  }

  @Override
  public void delete(MODEL t, Http.Request request) {
    preDelete(t, request);

    modelRepository.delete(t);

    postDelete(t, request);
  }

  protected void preDelete(MODEL t, Http.Request request) {
  }

  protected void postDelete(MODEL t, Http.Request request) {
    cache.removeByPrefix(cacheKeyGetter.apply(t.getId(), new String[0]));
  }

  @Override
  public void delete(Collection<MODEL> t, Http.Request request) {
    preDelete(t, request);

    modelRepository.delete(t);

    postDelete(t, request);
  }

  protected void preDelete(Collection<MODEL> t, Http.Request request) {
  }

  protected void postDelete(Collection<MODEL> t, Http.Request request) {
  }
}
