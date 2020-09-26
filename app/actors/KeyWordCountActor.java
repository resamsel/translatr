package actors;

import actors.WordCountProtocol.ChangeWordCount;
import akka.actor.AbstractActor;
import services.KeyService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
@Singleton
public class KeyWordCountActor extends AbstractActor {
  public static final String NAME = "key-word-count-actor";

  private final KeyService keyService;

  @Inject
  public KeyWordCountActor(KeyService keyService) {
    this.keyService = keyService;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(ChangeWordCount.class,
                    wordCount -> keyService.increaseWordCountBy(wordCount.id, wordCount.wordCountDiff, wordCount.request))
            .build();
  }
}
