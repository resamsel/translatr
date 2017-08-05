package services.impl;

import static java.util.Objects.requireNonNull;

import com.avaje.ebean.PagedList;
import criterias.AbstractSearchCriteria;
import java.util.Collection;
import java.util.function.BiFunction;
import javax.validation.Validator;
import models.Model;
import play.cache.CacheApi;
import repositories.ModelRepository;
import services.LogEntryService;
import services.ModelService;

/**
 * @author resamsel
 * @version 9 Sep 2016
 */
public abstract class AbstractModelService<MODEL extends Model<MODEL, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>>
    implements ModelService<MODEL, ID, CRITERIA> {

  protected final Validator validator;
  protected final LogEntryService logEntryService;
  protected final CacheApi cache;
  final ModelRepository<MODEL, ID, CRITERIA> modelRepository;
  private final BiFunction<ID, String[], String> cacheKeyGetter;

  public AbstractModelService(Validator validator, CacheApi cache,
      ModelRepository<MODEL, ID, CRITERIA> modelRepository,
      BiFunction<ID, String[], String> cacheKeyGetter, LogEntryService logEntryService) {
    this.validator = validator;
    this.cache = cache;
    this.modelRepository = modelRepository;
    this.cacheKeyGetter = cacheKeyGetter;
    this.logEntryService = logEntryService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<MODEL> findBy(CRITERIA criteria) {
    return cache.getOrElse(
        requireNonNull(criteria, "criteria is null").getCacheKey(),
        () -> modelRepository.findBy(criteria),
        60);
  }

  @Override
  public MODEL byId(ID id, String... fetches) {
    return cache.getOrElse(
        cacheKeyGetter.apply(id, fetches),
        () -> modelRepository.byId(id, fetches),
        60);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL create(MODEL model) {
    return modelRepository.create(model);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL update(MODEL model) {
    return modelRepository.update(model);
  }

  /**
   * {@inheritDoc}
   */
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
  }

  @Override
  public void delete(Collection<MODEL> t) {
    modelRepository.delete(t);
  }
}
