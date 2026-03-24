package com.translatr.event;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/**
 * Replaces MessageWordCountActorRef et al.
 */
@ApplicationScoped
public class WordCountEventProducer {

    @Inject EventBus bus;

    public void publishMessage(UUID messageId) {
        bus.publish("word-count", new WordCountEvent(WordCountEvent.Target.MESSAGE, messageId));
    }

    public void publishKey(UUID keyId) {
        bus.publish("word-count", new WordCountEvent(WordCountEvent.Target.KEY, keyId));
    }

    public void publishLocale(UUID localeId) {
        bus.publish("word-count", new WordCountEvent(WordCountEvent.Target.LOCALE, localeId));
    }

    public void publishProject(UUID projectId) {
        bus.publish("word-count", new WordCountEvent(WordCountEvent.Target.PROJECT, projectId));
    }
}
