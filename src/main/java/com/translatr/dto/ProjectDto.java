package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDto {
    public UUID    id;
    public Instant whenCreated;
    public Instant whenUpdated;
    public String  name;
    public String  description;
    public UUID    ownerId;
    public String  ownerName;
    public String  ownerUsername;
    public Integer wordCount;
    public Double  progress;
    public String  myRole;
}
