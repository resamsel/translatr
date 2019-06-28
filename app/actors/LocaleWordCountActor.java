package actors;

import actors.WordCountProtocol.ChangeWordCount;
import akka.actor.UntypedActor;
import services.LocaleService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
@Singleton
public class LocaleWordCountActor extends UntypedActor {
  public static final String NAME = "locale-word-count-actor";

  private final LocaleService localeService;

  @Inject
  public LocaleWordCountActor(LocaleService localeService) {
    this.localeService = localeService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof ChangeWordCount) {
      ChangeWordCount wordCount = (ChangeWordCount) msg;
      localeService.increaseWordCountBy(wordCount.id, wordCount.wordCountDiff);
    }
  }
}
