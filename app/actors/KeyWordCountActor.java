package actors;

import actors.WordCountProtocol.ChangeWordCount;
import akka.actor.UntypedActor;
import javax.inject.Inject;
import javax.inject.Singleton;
import services.KeyService;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
@Singleton
public class KeyWordCountActor extends UntypedActor {
  public static final String NAME = "key-word-count-actor";

  private final KeyService keyService;

  @Inject
  public KeyWordCountActor(KeyService keyService) {
    this.keyService = keyService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof ChangeWordCount) {
      ChangeWordCount wordCount = (ChangeWordCount) msg;
      keyService.increaseWordCountBy(wordCount.id, wordCount.wordCountDiff);
    }
  }
}
