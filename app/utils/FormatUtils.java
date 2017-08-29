package utils;

import models.Locale;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

public class FormatUtils {

  public static final String pretty(java.util.Locale locale, DateTime dateTime) {
    return new PrettyTime(locale).format(dateTime.toDate());
  }

  public static final String formatLocale(java.util.Locale userLocale, Locale locale) {
    return formatLocale(userLocale, locale.name);
  }

  public static final String formatLocale(java.util.Locale userLocale, String localeName) {
    java.util.Locale locale = java.util.Locale.forLanguageTag(localeName.replaceAll("_", "-"));

    if (locale.getDisplayName(userLocale).equalsIgnoreCase(localeName)) {
      return localeName;
    }

    return String.format("%s &mdash; %s", localeName, locale.getDisplayName(userLocale));
  }
}
