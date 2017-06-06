package utils;

import java.text.ParseException;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.joda.time.DateTime;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import play.i18n.MessagesApi;

/**
 * @author resamsel
 * @version 3 Nov 2016
 */
@Singleton
public class FormattersProvider implements Provider<Formatters> {
  private final MessagesApi messagesApi;

  @Inject
  public FormattersProvider(MessagesApi messagesApi) {
    this.messagesApi = messagesApi;
  }

  @Override
  public Formatters get() {
    Formatters formatters = new Formatters(messagesApi);

    formatters.register(DateTime.class, new SimpleFormatter<DateTime>() {
      @Override
      public DateTime parse(String in, Locale l) throws ParseException {
        return DateTime.parse(in);
      }

      @Override
      public String print(DateTime in, Locale l) {
        return in.toString();
      }
    });

    return formatters;
  }
}
