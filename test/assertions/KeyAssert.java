package assertions;

import models.Key;

public class KeyAssert extends AbstractGenericAssert<KeyAssert, Key> {

  private KeyAssert(Key actual) {
    super("key", actual, KeyAssert.class);
  }

  public static KeyAssert assertThat(Key actual) {
    return new KeyAssert(actual);
  }

  public KeyAssert nameIsEqualTo(String expected) {
    return isEqualTo("name", expected, actual.name);
  }
}
