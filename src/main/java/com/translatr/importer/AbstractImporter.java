package com.translatr.importer;

import com.translatr.model.Key;
import com.translatr.model.Locale;
import com.translatr.model.Message;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class AbstractImporter implements Importer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractImporter.class);

    protected final KeyRepository     keyRepo;
    protected final MessageRepository messageRepo;

    /** No-args constructor required by CDI for proxy generation. */
    protected AbstractImporter() {
        this.keyRepo     = null;
        this.messageRepo = null;
    }

    protected AbstractImporter(KeyRepository keyRepo, MessageRepository messageRepo) {
        this.keyRepo     = keyRepo;
        this.messageRepo = messageRepo;
    }

    @Override
    @Transactional
    public ImportResult apply(InputStream stream, Locale locale) throws Exception {
        LOG.debug("Importing into locale {}", locale.name);

        Properties props = parse(stream, locale);

        // Load existing keys and messages for this project/locale
        Map<String, Key> existingKeys = keyRepo.findByProject(locale.project.id)
                .stream().collect(Collectors.toMap(k -> k.name, k -> k));
        Map<String, Message> existingMessages = messageRepo
                .find("locale.id = ?1", locale.id)
                .stream().collect(Collectors.toMap(m -> m.key.name, m -> m));

        // Create missing keys
        List<Key> newKeys = new ArrayList<>();
        for (String keyName : props.stringPropertyNames()) {
            if (StringUtils.isEmpty(props.getProperty(keyName))) continue;
            if (!existingKeys.containsKey(keyName)) {
                Key k = new Key(locale.project, keyName);
                keyRepo.persist(k);
                existingKeys.put(keyName, k);
                newKeys.add(k);
            }
        }

        // Create/update messages
        int created = 0, updated = 0;
        for (String keyName : props.stringPropertyNames()) {
            String value = props.getProperty(keyName);
            if (StringUtils.isEmpty(value) || !existingKeys.containsKey(keyName)) continue;

            Key key = existingKeys.get(keyName);
            if (!existingMessages.containsKey(keyName)) {
                messageRepo.persist(new Message(locale, key, value));
                created++;
            } else {
                Message msg = existingMessages.get(keyName);
                if (!value.equals(msg.value)) {
                    msg.value = value;
                    updated++;
                }
            }
        }

        LOG.debug("Import done — keys: {}, messages created: {}, updated: {}",
                newKeys.size(), created, updated);
        return new ImportResult(newKeys.size(), created, updated);
    }

    /** Parse InputStream into a Properties map of key→value. */
    protected abstract Properties parse(InputStream stream, Locale locale) throws Exception;
}
