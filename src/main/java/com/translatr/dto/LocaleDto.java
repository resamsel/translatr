package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocaleDto {
    public UUID    id;
    public Instant whenCreated;
    public Instant whenUpdated;
    public UUID    projectId;
    public String  projectName;
    public String  projectOwnerUsername;
    public String  name;
    public String  displayName;
    public Double  progress;
    public Integer wordCount;
}
