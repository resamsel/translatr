package services.impl;

import com.avaje.ebean.PagedList;
import criterias.AbstractSearchCriteria;
import models.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  protected final CacheService cache;
  final ModelRepository<MODEL, ID, CRITERIA> modelRepository;
  private final BiFunction<ID, String[], String> cacheKeyGetter;

  public AbstractModelService(Validator validator, CacheService cache,
                              ModelRepository<MODEL, ID, CRITERIA> modelRepository,
                              BiFunction<ID, String[], String> cacheKeyGetter, LogEntryService logEntryService,
                              AuthProvider authProvider) {
    this.validator = validator;
    this.cache = cache;
    this.modelRepository = modelRepository;
    this.cacheKeyGetter = cacheKeyGetter;
    this.logEntryService = logEntryService;
    this.authProvider = authProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<MODEL> findBy(CRITERIA criteria) {
    criteria.setLoggedInUserId(authProvider.loggedInUserId());

    return postFind(cache.getOrElse(
            requireNonNull(criteria, "criteria is null").getCacheKey(),
            () -> modelRepository.findBy(criteria),
            60
    ));
  }

  protected PagedList<MODEL> postFind(PagedList<MODEL> pagedList) {
    return pagedList;
  }

  @Override
  public MODEL byId(ID id, String... fetches) {
    if (id == null) {
      return null;
    }

    return postGet(cache.getOrElse(
            cacheKeyGetter.apply(id, fetches),
            () -> modelRepository.byId(id, fetches),
            60
    ));
  }

  protected MODEL postGet(MODEL model) {
    return model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL create(MODEL model) {
    LOGGER.debug("create({})", model);

    preCreate(model);

    MODEL m = modelRepository.save(model);

    postCreate(m);

    return m;
  }

  protected void preCreate(MODEL t) {
  }

  protected void postCreate(MODEL t) {
    cache.set(cacheKeyGetter.apply(t.getId(), new String[0]), t, 60);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL update(MODEL t) {
    LOGGER.debug("update({})", t);

    MODEL m = modelRepository.update(t);

    postUpdate(m);

    return m;
  }

  protected MODEL postUpdate(MODEL t) {
    cache.removeByPrefix(cacheKeyGetter.apply(t.getId(), new String[0]));
    cache.set(cacheKeyGetter.apply(t.getId(), new String[0]), t, 60);

    return t;
  }

  @Override
  public MODEL save(MODEL t) {
    return modelRepository.save(t);
  }

  @Override
  public Collection<MODEL> save(Collection<MODEL> t) {
    return modelRepository.save(t);
  }

  @Override
  public void delete(MODEL t) {
    modelRepository.delete(t);

    postDelete(t);
  }

  @Override
  public void delete(Collection<MODEL> t) {
    modelRepository.delete(t);
  }

  protected void postDelete(MODEL t) {
    cache.removeByPrefix(cacheKeyGetter.apply(t.getId(), new String[0]));
  }
}
