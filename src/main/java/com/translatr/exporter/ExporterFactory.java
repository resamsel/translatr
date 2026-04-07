package com.translatr.exporter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class ExporterFactory {

    private final JavaPropertiesExporter javaProperties;
    private final PlayMessagesExporter   playMessages;
    private final JsonExporter           json;
    private final GettextExporter        gettext;

    @Inject
    public ExporterFactory(JavaPropertiesExporter javaProperties, PlayMessagesExporter playMessages,
                           JsonExporter json, GettextExporter gettext) {
        this.javaProperties = javaProperties;
        this.playMessages   = playMessages;
        this.json           = json;
        this.gettext        = gettext;
    }

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
