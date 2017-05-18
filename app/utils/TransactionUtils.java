package utils;

import java.util.function.Consumer;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

/**
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class TransactionUtils {
  /**
   * @param consumer
   * @throws Exception
   */
  public static void batchExecute(Consumer<Transaction> consumer) throws Exception {
    batchExecute(consumer, 200);
  }

  /**
   * @param consumer
   * @param batchSize
   * @throws Exception
   */
  public static void batchExecute(Consumer<Transaction> consumer, int batchSize) throws Exception {
    Transaction tx = Ebean.currentTransaction();

    if (tx != null) {
      // Use existing transaction and don't modify it
      consumer.accept(tx);
      return;
    }

    tx = Ebean.beginTransaction();
    try {
      tx.setBatchMode(true);
      tx.setBatchSize(batchSize);
      tx.setBatchGetGeneratedKeys(false);

      consumer.accept(tx);

      tx.commit();
    } finally {
      tx.end();
    }
  }

  public static interface CheckedConsumer<T> {
    void accept(T t) throws Exception;
  }

  /**
   * @param consumer
   * @param batchSize
   * @throws Exception
   */
  public static void execute(CheckedConsumer<Transaction> consumer) throws Exception {
    Transaction tx = Ebean.currentTransaction();

    if (tx != null) {
      // Use existing transaction and don't modify it
      consumer.accept(tx);
      return;
    }

    tx = Ebean.beginTransaction();
    try {
      consumer.accept(tx);

      tx.commit();
    } finally {
      tx.end();
    }
  }
}
