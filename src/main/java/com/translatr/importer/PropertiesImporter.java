package com.translatr.importer;

import com.translatr.model.Locale;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.MessageRepository;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public abstract class PropertiesImporter extends AbstractImporter {

    /** No-args constructor required by CDI for proxy generation. */
    protected PropertiesImporter() {}

    protected PropertiesImporter(KeyRepository keyRepo, MessageRepository messageRepo) {
        super(keyRepo, messageRepo);
    }

    @Override
    protected Properties parse(InputStream stream, Locale locale) throws Exception {
        Properties p = new Properties();
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        return p;
    }
}
