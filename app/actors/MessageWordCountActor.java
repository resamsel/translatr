package actors;

import actors.WordCountProtocol.ChangeMessageWordCount;
import actors.WordCountProtocol.ChangeWordCount;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

/**
 * @author resamsel
 * @version 6 Jun 2017
 */
@Singleton
public class MessageWordCountActor extends AbstractActor {
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

  @Override
  public Receive createReceive() {
    return receiveBuilder()
            .match(ChangeMessageWordCount.class, wordCount -> {
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
            })
            .match(Collection.class, t -> {
              Collection<ChangeMessageWordCount> wordCounts = (Collection<ChangeMessageWordCount>) t;

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
            })
            .build();
  }
}
