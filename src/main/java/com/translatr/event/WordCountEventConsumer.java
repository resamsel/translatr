package com.translatr.event;

import com.translatr.repository.KeyRepository;
import com.translatr.repository.LocaleRepository;
import com.translatr.repository.MessageRepository;
import com.translatr.repository.ProjectRepository;
import com.translatr.util.MessageUtils;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces MessageWordCountActor, LocaleWordCountActor, KeyWordCountActor,
 * ProjectWordCountActor (Akka) — recalculates word counts asynchronously.
 */
@ApplicationScoped
public class WordCountEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(WordCountEventConsumer.class);

    private final MessageRepository messageRepo;
    private final KeyRepository     keyRepo;
    private final LocaleRepository  localeRepo;
    private final ProjectRepository projectRepo;

    @Inject
    public WordCountEventConsumer(MessageRepository messageRepo, KeyRepository keyRepo,
                                  LocaleRepository localeRepo, ProjectRepository projectRepo) {
        this.messageRepo = messageRepo;
        this.keyRepo     = keyRepo;
        this.localeRepo  = localeRepo;
        this.projectRepo = projectRepo;
    }

    /**
     * {@code @Blocking} moves execution off the Vert.x IO thread — the
     * {@code @Transactional} JDBC work below otherwise throws
     * {@code BlockingOperationNotAllowedException} ("Cannot start a JTA transaction
     * from the IO thread"). Same fix as {@link ActivityEventConsumer#onActivity}.
     *
     * {@code ordered = true} so the MESSAGE/KEY/LOCALE/PROJECT events published for a
     * single message change are applied in send order: each roll-up level reads the
     * level below it ({@code key}/{@code locale} sum their messages, {@code project}
     * sums its locales), so the lower level must have been recalculated and committed
     * first. This mirrors the sequential mailbox of the old MessageWordCountActor.
     */
    @ConsumeEvent(value = "word-count", ordered = true)
    @Blocking
    @Transactional
    public void onWordCount(WordCountEvent event) {
        LOG.debug("onWordCount: target={} id={}", event.target, event.id);
        switch (event.target) {
            case MESSAGE -> messageRepo.findByIdOptional(event.id).ifPresent(m ->
                    m.wordCount = MessageUtils.wordCount(m.value));
            case KEY -> keyRepo.findByIdOptional(event.id).ifPresent(k ->
                    k.wordCount = sumMessageWordCounts("key.id = ?1", event.id));
            case LOCALE -> localeRepo.findByIdOptional(event.id).ifPresent(l ->
                    l.wordCount = sumMessageWordCounts("locale.id = ?1", event.id));
            case PROJECT -> projectRepo.findByIdOptional(event.id).ifPresent(p ->
                    p.wordCount = localeRepo.find("project.id = ?1", event.id).stream()
                            .mapToInt(l -> l.wordCount != null ? l.wordCount : 0).sum());
        }
    }

    private int sumMessageWordCounts(String query, Object param) {
        return messageRepo.find(query, param).stream()
                .mapToInt(m -> m.wordCount != null ? m.wordCount : 0).sum();
    }
}
