package actors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import actors.WordCountProtocol.ChangeMessageWordCount;
import actors.WordCountProtocol.ChangeWordCount;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
@Singleton
public class MessageWordCountActor extends UntypedActor {
  public static final String NAME = "message-word-count-actor";

  private final ActorRef localeWordCountActor;

  private final ActorRef keyWordCountActor;

  private final ActorRef projectWordCountActor;

  @Inject
  public MessageWordCountActor(@Named(LocaleWordCountActor.NAME) ActorRef localeWordCountActor,
      @Named(KeyWordCountActor.NAME) ActorRef keyWordCountActor,
      @Named(ProjectWordCountActor.NAME) ActorRef projectWordCountActor) {
    this.localeWordCountActor = localeWordCountActor;
    this.keyWordCountActor = keyWordCountActor;
    this.projectWordCountActor = projectWordCountActor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof ChangeMessageWordCount) {
      ChangeMessageWordCount wordCount = (ChangeMessageWordCount) msg;

      if (wordCount.projectId != null)
        projectWordCountActor.tell(
            new ChangeWordCount(wordCount.projectId, wordCount.wordCount, wordCount.wordCountDiff),
            self());
      if (wordCount.localeId != null)
        localeWordCountActor.tell(
            new ChangeWordCount(wordCount.localeId, wordCount.wordCount, wordCount.wordCountDiff),
            self());
      if (wordCount.keyId != null)
        keyWordCountActor.tell(
            new ChangeWordCount(wordCount.keyId, wordCount.wordCount, wordCount.wordCountDiff),
            self());
    } else if (msg instanceof Collection) {
      @SuppressWarnings("unchecked")
      Collection<ChangeMessageWordCount> wordCounts = (Collection<ChangeMessageWordCount>) msg;

      wordCounts.stream()
          .map(wc -> new ChangeWordCount(wc.projectId, wc.wordCount, wc.wordCountDiff))
          .collect(groupingBy(wc -> wc.id, reducing(ChangeWordCount::merge)))
          .forEach((projectId, wc) -> projectWordCountActor.tell(wc.get(), null));
      wordCounts.stream()
          .map(wc -> new ChangeWordCount(wc.localeId, wc.wordCount, wc.wordCountDiff))
          .collect(groupingBy(wc -> wc.id, reducing(ChangeWordCount::merge)))
          .forEach((localeId, wc) -> localeWordCountActor.tell(wc.get(), null));
      wordCounts.stream().map(wc -> new ChangeWordCount(wc.keyId, wc.wordCount, wc.wordCountDiff))
          .collect(groupingBy(wc -> wc.id, reducing(ChangeWordCount::merge)))
          .forEach((keyId, wc) -> keyWordCountActor.tell(wc.get(), null));
    }
  }
}
