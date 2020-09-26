package repositories;

import io.ebean.PagedList;
import criterias.AbstractSearchCriteria;
import criterias.GetCriteria;
import models.Model;

import java.util.Collection;

public interface ModelRepository<T extends Model<T, ID>, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>> {
  PagedList<T> findBy(CRITERIA criteria);

  /**
   * @deprecated use byId(GetCriteria) instead
   */
  @Deprecated
  T byId(ID id, String... propertiesToFetch);

  T byId(GetCriteria<ID> criteria);

  /**
   * Create a new model in the database. Eventually, the {@link ModelRepository#save(Model)} method will be invoked.
   *
   * @param model the model including the ID
   * @return the created model
   * @throws javax.validation.ValidationException when either the ID is not unique, or the validation of the entity
   *                                              fails
   */
  T create(T model);

  /**
   * Update existing model in the database. If the model doesn't exist, a validation exception will be thrown.
   * Eventually, the {@link ModelRepository#save(Model)} method will be invoked.
   *
   * @param model the model including the ID
   * @return the updated model
   * @throws javax.validation.ValidationException when either the ID is missing, the entity doesn't exist in the
   *                                              database, or the validation of the entity fails
   */
  T update(T model);

  /**
   * Save or update existing model in the database. Decides on the existence of the ID whether or not the entity needs
   * to be saved or updated.
   *
   * @param model the model
   * @return the saved model
   * @throws javax.validation.ValidationException when the validation of the entity fails
   */
  T save(T model);

  /**
   * Persist model collection to database.
   */
  Collection<T> save(Collection<T> t);

  /**
   * Persist model to database, ignoring any pre/post save methods.
   */
  T persist(T t);

  void delete(T t);

  void delete(Collection<T> t);

}
