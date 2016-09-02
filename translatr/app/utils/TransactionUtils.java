package utils;

import java.util.function.Consumer;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class TransactionUtils
{
	/**
	 * @param consumer
	 * @throws Exception
	 */
	public static void batchExecute(Consumer<Transaction> consumer) throws Exception
	{
		batchExecute(consumer, 200);
	}

	/**
	 * @param consumer
	 * @param batchSize
	 * @throws Exception
	 */
	public static void batchExecute(Consumer<Transaction> consumer, int batchSize) throws Exception
	{
		Transaction tx = Ebean.currentTransaction();

		if(tx != null)
		{
			// Use existing transaction and don't modify it
			consumer.accept(tx);
			return;
		}

		tx = Ebean.beginTransaction();
		try
		{
			tx.setBatchMode(true);
			tx.setBatchSize(200);
			tx.setBatchGetGeneratedKeys(false);

			consumer.accept(tx);

			tx.commit();
		}
		finally
		{
			tx.end();
		}
	}
}
