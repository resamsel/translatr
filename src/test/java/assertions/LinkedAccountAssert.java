package assertions;

import models.LinkedAccount;

public class LinkedAccountAssert extends AbstractGenericAssert<LinkedAccountAssert, LinkedAccount> {

  private LinkedAccountAssert(LinkedAccount actual) {
    super("linkedAccount", actual, LinkedAccountAssert.class);
  }

  public static LinkedAccountAssert assertThat(LinkedAccount actual) {
    return new LinkedAccountAssert(actual);
  }

  public LinkedAccountAssert providerKeyIsEqualTo(String expected) {
    return isEqualTo("providerKey", expected, actual.providerKey);
  }
}
