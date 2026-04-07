package com.translatr.event;

import com.translatr.repository.KeyRepository;
import com.translatr.repository.LocaleRepository;
import com.translatr.repository.MessageRepository;
import com.translatr.repository.ProjectRepository;
import io.quarkus.vertx.ConsumeEvent;
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

    @ConsumeEvent("word-count")
    @Transactional
    public void onWordCount(WordCountEvent event) {
        LOG.debug("onWordCount: target={} id={}", event.target, event.id);
        switch (event.target) {
            case MESSAGE -> messageRepo.findByIdOptional(event.id).ifPresent(m -> {
                m.wordCount = m.value != null ? m.value.split("\\s+").length : 0;
            });
            case KEY -> keyRepo.findByIdOptional(event.id).ifPresent(k -> {
                k.wordCount = messageRepo.find("key.id = ?1", event.id).stream()
                        .mapToInt(m -> m.wordCount != null ? m.wordCount : 0).sum();
            });
            case LOCALE -> localeRepo.findByIdOptional(event.id).ifPresent(l -> {
                l.wordCount = messageRepo.find("locale.id = ?1", event.id).stream()
                        .mapToInt(m -> m.wordCount != null ? m.wordCount : 0).sum();
            });
            case PROJECT -> projectRepo.findByIdOptional(event.id).ifPresent(p -> {
                p.wordCount = localeRepo.find("project.id = ?1", event.id).stream()
                        .mapToInt(l -> l.wordCount != null ? l.wordCount : 0).sum();
            });
        }
    }
}
