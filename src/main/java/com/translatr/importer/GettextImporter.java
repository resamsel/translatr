package com.translatr.importer;

import com.translatr.model.Locale;
import com.translatr.repository.KeyRepository;
import com.translatr.repository.MessageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight PO-file parser (replaces GettextResourceBundle dependency).
 * Handles msgid/msgstr pairs; ignores comments, plural forms, and headers.
 */
@ApplicationScoped
public class GettextImporter extends AbstractImporter {

    private static final Pattern MSGID  = Pattern.compile("^msgid\s+\"(.*)\"$");
    private static final Pattern MSGSTR = Pattern.compile("^msgstr\s+\"(.*)\"$");

    @Inject
    public GettextImporter(KeyRepository keyRepo, MessageRepository messageRepo) {
        super(keyRepo, messageRepo);
    }

    @Override
    protected Properties parse(InputStream stream, Locale locale) throws Exception {
        Properties props = new Properties();
        String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        String currentId  = null;
        for (String line : content.split("\n")) {
            line = line.strip();
            Matcher id  = MSGID.matcher(line);
            Matcher str = MSGSTR.matcher(line);
            if (id.matches()) {
                currentId = unescape(id.group(1));
            } else if (str.matches() && currentId != null && !currentId.isEmpty()) {
                String value = unescape(str.group(1));
                if (!value.isEmpty()) props.setProperty(currentId, value);
                currentId = null;
            }
        }
        return props;
    }

    private String unescape(String s) {
        return s.replace("\\n", "\n").replace("\\t", "\t").replace("\\\"", "\"");
    }
}
