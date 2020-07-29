package assertions;

import dto.Locale;

public class LocaleDtoAssert extends AbstractGenericAssert<LocaleDtoAssert, Locale> {

  LocaleDtoAssert(Locale actual) {
    super("locale", actual, LocaleDtoAssert.class);
  }

  public LocaleDtoAssert displayNameIsEqualTo(String expected) {
    return isEqualTo("displayName", expected, actual.displayName);
  }
}
