package services.impl;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import models.Model;
import play.Configuration;
import play.mvc.Http.Context;
import play.mvc.Http.Session;
import services.LogEntryService;
import services.ModelService;
import utils.TransactionUtils;

/**
 *
 * @author resamsel
 * @version 9 Sep 2016
 */
public abstract class AbstractModelService<MODEL extends Model<MODEL, ID>, ID>
    implements ModelService<MODEL> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelService.class);

  protected final Configuration configuration;

  protected final Validator validator;

  protected final LogEntryService logEntryService;


  /**
   * @param configuration
   */
  public AbstractModelService(Configuration configuration, Validator validator,
      LogEntryService logEntryService) {
    this.configuration = configuration;
    this.validator = validator;
    this.logEntryService = logEntryService;
  }

  /**
   * Shorthand for context.current.session.
   * 
   * @return
   */
  protected Session session() {
    return Context.current().session();
  }

  protected abstract MODEL byId(ID id);

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL create(MODEL model) {
    try {
      return save(model);
    } catch (PersistenceException e) {
      if (e.getCause() != null && e.getCause() instanceof SQLException
          && "23505".equals(((PSQLException) e.getCause()).getSQLState()))
        throw new ValidationException("Entry already exists (duplicate key)");

      throw new ValidationException(e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL update(MODEL model) {
    if (model.getId() == null)
      throw new ValidationException("Field 'id' required");

    MODEL m = byId(model.getId());

    if (m == null)
      throw new ValidationException(String.format("Entity with ID '%s' not found", model.getId()));

    return save(m.updateFrom(model));
  }

  /**
   * @param dto
   */
  protected MODEL validate(MODEL model) {
    Set<ConstraintViolation<MODEL>> violations = validator.validate(model);

    if (!violations.isEmpty())
      throw new ConstraintViolationException("Constraint violations detected", violations);

    return model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL save(MODEL t) {
    boolean update = !Ebean.getBeanState(t).isNew();

    validate(t);

    preSave(t, update);

    Ebean.save(t);
    // Ebean.refresh(t);

    postSave(t, update);

    return t;
  }

  /**
   * @param t
   */
  protected void preSave(MODEL t, boolean update) {}

  /**
   * @param t
   */
  protected void postSave(MODEL t, boolean update) {}

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<MODEL> save(Collection<MODEL> t) {
    try {
      preSave(t);
      TransactionUtils.batchExecute((tx) -> {
        Ebean.saveAll(t);
      });
      postSave(t);
    } catch (Exception e) {
      LOGGER.error("Error while batch saving entities", e);
    }

    return t;
  }

  /**
   * @param t
   */
  protected void preSave(Collection<MODEL> t) {}

  /**
   * @param t
   */
  protected void postSave(Collection<MODEL> t) {}

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(MODEL t) {
    preDelete(t);
    Ebean.delete(t);
    postDelete(t);
  }

  /**
   * @param t
   */
  protected void preDelete(MODEL t) {}

  /**
   * @param t
   */
  protected void postDelete(MODEL t) {}

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Collection<MODEL> t) {
    try {
      preDelete(t);
      TransactionUtils.batchExecute((tx) -> {
        Ebean.deleteAll(t);
      });
      postDelete(t);
    } catch (Exception e) {
      LOGGER.error("Error while batch deleting entities", e);
    }
  }

  /**
   * @param t
   */
  protected void preDelete(Collection<MODEL> t) {}

  /**
   * @param t
   */
  protected void postDelete(Collection<MODEL> t) {}
}
