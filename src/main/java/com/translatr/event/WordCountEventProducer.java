package com.translatr.event;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Replaces MessageWordCountActorRef et al.
 */
@ApplicationScoped
public class WordCountEventProducer {

    private final EventBus bus;

    public WordCountEventProducer(EventBus bus) {
        this.bus = bus;
    }

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
