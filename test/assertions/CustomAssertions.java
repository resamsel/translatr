package assertions;

import models.Locale;
import play.mvc.Result;

public class CustomAssertions {

  public static LocaleAssert assertThat(Locale actual) {
    return new LocaleAssert(actual);
  }

  public static LocaleDtoAssert assertThat(dto.Locale actual) {
    return new LocaleDtoAssert(actual);
  }

  public static ResultAssert assertThat(Result actual) {
    return new ResultAssert(actual);
  }
}
