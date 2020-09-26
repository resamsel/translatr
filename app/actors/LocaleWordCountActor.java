package actors;

import actors.WordCountProtocol.ChangeWordCount;
import akka.actor.AbstractActor;
import services.LocaleService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
@Singleton
public class LocaleWordCountActor extends AbstractActor {
  public static final String NAME = "locale-word-count-actor";

  private final LocaleService localeService;

  @Inject
  public LocaleWordCountActor(LocaleService localeService) {
    this.localeService = localeService;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(ChangeWordCount.class,
                    wordCount -> localeService.increaseWordCountBy(wordCount.id, wordCount.wordCountDiff, null /* FIXME */))
            .build();
  }
}
