package com.translatr.importer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class ImporterFactory {

    private final JavaPropertiesImporter javaProperties;
    private final PlayMessagesImporter   playMessages;
    private final JsonImporter           json;
    private final GettextImporter        gettext;

    @Inject
    public ImporterFactory(JavaPropertiesImporter javaProperties, PlayMessagesImporter playMessages,
                           JsonImporter json, GettextImporter gettext) {
        this.javaProperties = javaProperties;
        this.playMessages   = playMessages;
        this.json           = json;
        this.gettext        = gettext;
    }

    public Importer forFileType(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "java_properties" -> javaProperties;
            case "play_messages"   -> playMessages;
            case "json"            -> json;
            case "gettext"         -> gettext;
            default -> throw new BadRequestException("Unknown file type: " + fileType);
        };
    }
}
