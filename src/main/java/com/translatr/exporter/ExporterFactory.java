package com.translatr.exporter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class ExporterFactory {

    @Inject JavaPropertiesExporter javaProperties;
    @Inject PlayMessagesExporter   playMessages;
    @Inject JsonExporter           json;
    @Inject GettextExporter        gettext;

    public Exporter forFileType(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "java_properties" -> javaProperties;
            case "play_messages"   -> playMessages;
            case "json"            -> json;
            case "gettext"         -> gettext;
            default -> throw new BadRequestException("Unknown file type: " + fileType);
        };
    }
}
