package services.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import services.ModelService;
import utils.TransactionUtils;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 9 Sep 2016
 */
public abstract class AbstractModelService<T> implements ModelService<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelService.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T save(T t)
	{
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
	protected void preSave(T t, boolean update)
	{
	}

	/**
	 * @param t
	 */
	protected void postSave(T t, boolean update)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<T> save(Collection<T> t)
	{
		try
		{
			preSave(t);
			TransactionUtils.batchExecute((tx) -> {
				Ebean.saveAll(t);
			});
			postSave(t);
		}
		catch(Exception e)
		{
			LOGGER.error("Error while batch saving entities", e);
		}

		return t;
	}

	/**
	 * @param t
	 */
	protected void preSave(Collection<T> t)
	{
	}

	/**
	 * @param t
	 */
	protected void postSave(Collection<T> t)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(T t)
	{
		preDelete(t);
		Ebean.delete(t);
		postDelete(t);
	}

	/**
	 * @param t
	 */
	protected void preDelete(T t)
	{
	}

	/**
	 * @param t
	 */
	protected void postDelete(T t)
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Collection<T> t)
	{
		try
		{
			preDelete(t);
			TransactionUtils.batchExecute((tx) -> {
				Ebean.deleteAll(t);
			});
			postDelete(t);
		}
		catch(Exception e)
		{
			LOGGER.error("Error while batch deleting entities", e);
		}
	}

	/**
	 * @param t
	 */
	protected void preDelete(Collection<T> t)
	{
	}

	/**
	 * @param t
	 */
	protected void postDelete(Collection<T> t)
	{
	}
}
