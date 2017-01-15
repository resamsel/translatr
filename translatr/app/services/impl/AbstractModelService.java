package services.impl;

import java.util.Collection;

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

  protected final LogEntryService logEntryService;


  /**
   * @param configuration
   */
  public AbstractModelService(Configuration configuration, LogEntryService logEntryService) {
    this.configuration = configuration;
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
    return save(validate(model));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL update(MODEL model) {
    MODEL m = byId(model.getId()).updateFrom(validate(model));

    return save(m);
  }

  /**
   * @param dto
   */
  protected MODEL validate(MODEL model) {
    return model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MODEL save(MODEL t) {
    boolean update = !Ebean.getBeanState(t).isNew();
    preSave(t, update);
    Ebean.save(t);
    Ebean.refresh(t);
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
