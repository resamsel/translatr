package utils;

import io.ebean.Ebean;
import io.ebean.Transaction;

import java.util.function.Consumer;

/**
 *
 * @author resamsel
 * @version 2 Sep 2016
 */
public class TransactionUtils {
  public static void batchExecute(Consumer<Transaction> consumer) throws Exception {
    batchExecute(consumer, 200);
  }

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

  public interface CheckedConsumer<T> {
    void accept(T t) throws Exception;
  }

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
