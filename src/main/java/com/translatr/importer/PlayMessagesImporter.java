package com.translatr.importer;

import com.translatr.repository.KeyRepository;
import com.translatr.repository.MessageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PlayMessagesImporter extends PropertiesImporter {

    /** No-args constructor required by CDI for proxy generation. */
    protected PlayMessagesImporter() {}

    @Inject
    public PlayMessagesImporter(KeyRepository keyRepo, MessageRepository messageRepo) {
        super(keyRepo, messageRepo);
    }
}
