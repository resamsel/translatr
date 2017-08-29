package assertions;

import models.Locale;
import models.Message;

public class LocaleAssert extends AbstractGenericAssert<LocaleAssert, Locale> {

  private LocaleAssert(Locale actual) {
    super("locale", actual, LocaleAssert.class);
  }

  public static LocaleAssert assertThat(Locale actual) {
    return new LocaleAssert(actual);
  }

  public LocaleAssert nameIsEqualTo(String expected) {
    return isEqualTo("name", expected, actual.name);
  }
}
