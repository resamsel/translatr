package com.translatr.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.translatr.model.Key;
import com.translatr.model.Locale;
import com.translatr.model.Message;
import com.translatr.model.Project;
import com.translatr.model.User;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.LocaleRepository;
import com.translatr.repository.MessageRepository;
import com.translatr.repository.ProjectRepository;
import com.translatr.repository.UserRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WordCountEventConsumerTest {

    @Inject WordCountEventProducer producer;
    @Inject MessageRepository      messageRepo;
    @Inject KeyRepository          keyRepo;
    @Inject LocaleRepository       localeRepo;
    @Inject ProjectRepository      projectRepo;
    @Inject UserRepository         userRepo;

    @Test
    void publishingMessageEvent_recalculatesTheMessageWordCount() throws InterruptedException {
        Graph g = createGraph("one two three four", null);

        producer.publishMessage(g.messageId);

        Integer wc = await(() -> messageRepo.findById(g.messageId).wordCount);
        assertThat(wc).as("message word count").isEqualTo(4);
    }

    @Test
    void publishingKeyEvent_sumsTheWordCountsOfItsMessages() throws InterruptedException {
        Graph g = createGraph("alpha beta", 2);

        producer.publishKey(g.keyId);

        Integer wc = await(() -> keyRepo.findById(g.keyId).wordCount);
        assertThat(wc).as("key word count = sum of its messages").isEqualTo(2);
    }

    @Test
    void publishingLocaleEvent_sumsTheWordCountsOfItsMessages() throws InterruptedException {
        Graph g = createGraph("uno dos tres", 3);

        producer.publishLocale(g.localeId);

        Integer wc = await(() -> localeRepo.findById(g.localeId).wordCount);
        assertThat(wc).as("locale word count = sum of its messages").isEqualTo(3);
    }

    @Test
    void publishingProjectEvent_sumsTheWordCountsOfItsLocales() throws InterruptedException {
        Graph g = createGraph("one two three", 3);
        // the project roll-up sums locale word counts, so seed the locale first
        QuarkusTransaction.requiringNew().run(() -> localeRepo.findById(g.localeId).wordCount = 3);

        producer.publishProject(g.projectId);

        Integer wc = await(() -> projectRepo.findById(g.projectId).wordCount);
        assertThat(wc).as("project word count = sum of its locales").isEqualTo(3);
    }

    // --- helpers -----------------------------------------------------------------

    private record Graph(UUID projectId, UUID localeId, UUID keyId, UUID messageId) {}

    /** Persist a project / locale / key / message graph in its own committed transaction. */
    private Graph createGraph(String messageValue, Integer messageWordCount) {
        return QuarkusTransaction.requiringNew().call(() -> {
            User u = new User();
            u.username = "wc-" + System.nanoTime();
            u.name     = "Word Count Test";
            userRepo.persist(u);

            Project p = new Project("wc-proj-" + System.nanoTime());
            p.owner = u;
            projectRepo.persist(p);

            Locale l = new Locale(p, "de");
            localeRepo.persist(l);

            Key k = new Key(p, "wc-key-" + System.nanoTime());
            keyRepo.persist(k);

            Message m = new Message(l, k, messageValue);
            m.wordCount = messageWordCount;
            messageRepo.persist(m);

            return new Graph(p.id, l.id, k.id, m.id);
        });
    }

    /** Poll (in a fresh transaction each time) until the consumer has written a value. */
    private <T> T await(Supplier<T> read) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            T value = QuarkusTransaction.requiringNew().call(() -> read.get());
            if (value != null) {
                return value;
            }
            Thread.sleep(100);
        }
        return null;
    }
}
