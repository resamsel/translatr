package i18n;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import play.api.Configuration;
import play.api.Environment;
import play.api.i18n.DefaultMessagesApi;
import play.api.i18n.Lang;
import play.api.i18n.Langs;
import scala.collection.Seq;
import utils.ContextKey;

/**
 *
 * @author resamsel
 * @version 31 Aug 2016
 */
public class TranslatrMessagesApi extends DefaultMessagesApi {
  /**
   * @param environment
   * @param configuration
   * @param langs
   */
  @Inject
  public TranslatrMessagesApi(Environment environment, Configuration configuration, Langs langs) {
    super(environment, configuration, langs);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String noMatch(String key, Seq<Object> args, Lang lang) {
    addUndefined(key);
    return super.noMatch(key, args, lang);
  }

  private static void addUndefined(String key) {
    Set<String> undefined = ContextKey.UndefinedMessages.get();
    if (undefined == null)
      undefined = ContextKey.UndefinedMessages.put(new HashSet<>());
    undefined.add(key);
  }
}
