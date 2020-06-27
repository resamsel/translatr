package i18n;

import play.api.Configuration;
import play.api.Environment;
import play.api.i18n.DefaultMessagesApi;
import play.api.i18n.Lang;
import play.api.i18n.Langs;
import scala.collection.Seq;
import services.ContextProvider;
import utils.ContextKey;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * @author resamsel
 * @version 31 Aug 2016
 */
public class TranslatrMessagesApi extends DefaultMessagesApi {
  private final ContextProvider contextProvider;

  @Inject
  public TranslatrMessagesApi(Environment environment, Configuration configuration, Langs langs,
                              ContextProvider contextProvider) {
    super(environment, configuration, langs);
    this.contextProvider = contextProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String noMatch(String key, Seq<Object> args, Lang lang) {
    addUndefined(key);
    return super.noMatch(key, args, lang);
  }

  private void addUndefined(String key) {
    Set<String> undefined = ContextKey.UndefinedMessages.get(contextProvider.getOrNull());
    if (undefined == null)
      undefined = ContextKey.UndefinedMessages.put(new HashSet<>());
    undefined.add(key);
  }
}
