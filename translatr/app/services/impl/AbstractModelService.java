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
	public Collection<T> save(Collection<T> t)
	{
		try
		{
			TransactionUtils.batchExecute((tx) -> {
				Ebean.saveAll(t);
			});
		}
		catch(Exception e)
		{
			LOGGER.error("Error while batch saving log entries", e);
		}

		return t;
	}
}
