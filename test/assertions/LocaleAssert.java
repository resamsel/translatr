package assertions;

import models.Locale;

public class LocaleAssert extends AbstractGenericAssert<LocaleAssert, Locale> {

    LocaleAssert(Locale actual) {
        super("locale", actual, LocaleAssert.class);
    }

    public LocaleAssert nameIsEqualTo(String expected) {
        return isEqualTo("name", expected, actual.name);
    }
}
