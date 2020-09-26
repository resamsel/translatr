package utils;

import models.Locale;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FormatUtils {
  private final MessagesApi messagesApi;

  @Inject
  public FormatUtils(MessagesApi messagesApi) {
    this.messagesApi = messagesApi;
  }

  public static String pretty(java.util.Locale locale, DateTime dateTime) {
    return new PrettyTime(locale).format(dateTime.toDate());
  }

  public static String formatLocale(java.util.Locale userLocale, Locale locale) {
    return formatLocale(userLocale, locale.name);
  }

  public static String formatLocale(java.util.Locale userLocale, String localeName) {
    java.util.Locale locale = java.util.Locale.forLanguageTag(localeName.replaceAll("_", "-"));

    if (locale.getDisplayName(userLocale).equalsIgnoreCase(localeName)) {
      return localeName;
    }

    return String.format("%s &mdash; %s", localeName, locale.getDisplayName(userLocale));
  }

  public String formatDisplayName(Locale locale, Http.Request request) {
    if (StringUtils.isEmpty(locale.name)) {
      return null;
    }

    java.util.Locale l = java.util.Locale.forLanguageTag(locale.name);
    if (request == null) {
      return l.getDisplayName();
    }

    Messages messages = messagesApi.preferred(request);
    java.util.Locale ctxLocale = messages.lang().locale();

    return l.getDisplayName(ctxLocale);
  }
}
