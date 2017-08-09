package utils;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import utils.TransactionUtils;

public class TransactionUtilsTest {
  @Test
  public void instanceOf() {
    assertThat(new TransactionUtils()).isNotNull();
  }
}
