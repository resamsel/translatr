package utils;

import models.Locale;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;

public class FormatUtils {

  private static final Map<Http.Context, java.util.Locale> CONTEXT_LOCALE_MAP = new HashMap<>();

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

  public static String formatDisplayName(Locale locale) {
    if (StringUtils.isEmpty(locale.name)) {
      return null;
    }

    java.util.Locale l = java.util.Locale.forLanguageTag(locale.name);

    Http.Context ctx = Http.Context.current.get();
    if (ctx == null) {
      return l.getDisplayName();
    }

    java.util.Locale ctxLocale = CONTEXT_LOCALE_MAP
        .computeIfAbsent(ctx, context -> context.lang().locale());

    return l.getDisplayName(ctxLocale);
  }
}
