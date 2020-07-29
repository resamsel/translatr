package tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TransactionUtils;

public abstract class AbstractDatabaseTest extends AbstractTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseTest.class);

  @Override
  public void startPlay() {
    super.startPlay();

    cleanDatabase();
  }

  private void cleanDatabase() {
    try {
      TransactionUtils.execute(tx -> {
        LOGGER.info("Deleting all users");
        tx.getConnection().createStatement().executeUpdate("truncate user_ cascade");
      });
    } catch (Exception e) {
      LOGGER.error("Error while cleaning database", e);
    }
  }
}
