package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto {
    public UUID    id;
    public Instant whenCreated;
    public Instant whenUpdated;
    public UUID    localeId;
    public String  localeName;
    // Human-readable name of localeName rendered in the viewer's language, e.g. "de" -> "German".
    // Derived from localeName; stamped by MessageService on read paths, not by DtoMapper.
    public String  localeDisplayName;
    public UUID    keyId;
    public String  keyName;
    public UUID    projectId;
    public String  projectName;
    public String  value;
    public Integer wordCount;
}
