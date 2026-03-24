package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityDto {
    public UUID    id;
    public String  type;
    public String  contentType;
    public Instant whenCreated;
    public UUID    userId;
    public String  userName;
    public String  userUsername;
    public UUID    projectId;
    public String  projectName;
    public String  before;
    public String  after;
}
