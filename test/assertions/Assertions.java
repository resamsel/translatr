package assertions;

import models.Locale;

public class Assertions {
    public static LocaleAssert assertThat(Locale actual) {
      return new LocaleAssert(actual);
    }

    public static LocaleDtoAssert assertThat(dto.Locale actual) {
        return new LocaleDtoAssert(actual);
    }
}
