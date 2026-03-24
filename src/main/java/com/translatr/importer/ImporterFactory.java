package com.translatr.importer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class ImporterFactory {

    @Inject JavaPropertiesImporter javaProperties;
    @Inject PlayMessagesImporter   playMessages;
    @Inject JsonImporter           json;
    @Inject GettextImporter        gettext;

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
