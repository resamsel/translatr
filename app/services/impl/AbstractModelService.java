package services.impl;

import static java.util.Objects.requireNonNull;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import criterias.AbstractSearchCriteria;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import models.Model;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import repositories.ModelRepository;
import services.LogEntryService;
import services.ModelService;
import utils.TransactionUtils;

/**
 * @author resamsel
 * @version 9 Sep 2016
 */
public abstract class AbstractModelService<MODEL extends Model<MODEL, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>>
    implements ModelService<MODEL, ID, CRITERIA> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelService.class);

  protected final Validator validator;
  protected final LogEntryService logEntryService;
  protected final CacheApi cache;
  final ModelRepository<MODEL, ID, CRITERIA> modelRepository;
  final BiFunction<ID, String[], String> cacheKeyGetter;

  public AbstractModelService(Validator validator, LogEntryService logEntryService) {
    this(validator, null, null, null, logEntryService);
  }

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
    if (modelRepository == null) {
      throw new UnsupportedOperationException("Method findBy not implemented");
    }

    return cache.getOrElse(
        requireNonNull(criteria, "criteria is null").getCacheKey(),
        () -> modelRepository.findBy(criteria),
        60);
  }

  @Override
  public MODEL byId(ID id, String... fetches) {
    return cache.getOrElse(
        requireNonNull(cacheKeyGetter, "cacheKeyGetter is null").apply(id, fetches),
        () -> requireNonNull(modelRepository, "modelRepository is null").byId(id, fetches),
        60);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL create(MODEL model) {
    if (modelRepository != null) {
      return modelRepository.create(model);
    }

    try {
      return save(model);
    } catch (PersistenceException e) {
      if (e.getCause() != null && e.getCause() instanceof PSQLException
          && "23505".equals(((PSQLException) e.getCause()).getSQLState())) {
        throw new ValidationException("Entry already exists (duplicate key)");
      }

      throw new ValidationException(e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL update(MODEL model) {
    if (modelRepository != null) {
      return modelRepository.update(model);
    }

    if (model.getId() == null) {
      throw new ValidationException("Field 'id' required");
    }

    MODEL m = byId(model.getId());

    if (m == null) {
      throw new ValidationException(String.format("Entity with ID '%s' not found", model.getId()));
    }

    return save(m.updateFrom(model));
  }

  protected MODEL validate(MODEL model) {
    Set<ConstraintViolation<MODEL>> violations = validator.validate(model);

    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(
          "Constraint violations detected: " + violations.stream().map(Object::toString).collect(
              Collectors.joining(",")), violations);
    }

    return model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL save(MODEL t) {
    if (modelRepository != null) {
      return modelRepository.save(t);
    }

    boolean update = !Ebean.getBeanState(t).isNew();

    preSave(t, update);

    validate(t);

    prePersist(t, update);

    persist(t);

    postSave(t, update);

    return t;
  }

  /**
   * Persist model to database, ignoring any pre/post save methods.
   */
  protected MODEL persist(MODEL t) {
    Ebean.save(t);
    // Ebean.refresh(t);
    return t;
  }

  protected void preSave(MODEL t, boolean update) {
  }

  protected void prePersist(MODEL t, boolean update) {
  }

  protected void postSave(MODEL t, boolean update) {
  }

  @Override
  public Collection<MODEL> save(Collection<MODEL> t) {
    if (modelRepository != null) {
      return modelRepository.save(t);
    }

    try {
      preSave(t);
      persist(t);
      postSave(t);
    } catch (Exception e) {
      LOGGER.error("Error while batch saving entities", e);
      throw new PersistenceException(e);
    }

    return t;
  }

  protected Collection<MODEL> persist(Collection<MODEL> t) throws Exception {
    TransactionUtils.batchExecute((tx) -> Ebean.saveAll(t));

    return t;
  }

  protected void preSave(Collection<MODEL> t) {
  }

  protected void postSave(Collection<MODEL> t) {
  }

  @Override
  public void delete(MODEL t) {
    if (modelRepository != null) {
      modelRepository.delete(t);
      return;
    }

    preDelete(t);
    Ebean.delete(t);
    postDelete(t);
  }

  protected void preDelete(MODEL t) {
  }

  protected void postDelete(MODEL t) {
  }

  @Override
  public void delete(Collection<MODEL> t) {
    if (modelRepository != null) {
      modelRepository.delete(t);
      return;
    }

    try {
      preDelete(t);
      TransactionUtils.batchExecute((tx) -> Ebean.deleteAll(t));
      postDelete(t);
    } catch (Exception e) {
      LOGGER.error("Error while batch deleting entities", e);
      throw new PersistenceException(e);
    }
  }

  protected void preDelete(Collection<MODEL> t) {
  }

  protected void postDelete(Collection<MODEL> t) {
  }
}
