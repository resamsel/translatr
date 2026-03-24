package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberDto {
    public Long    id;
    public Instant whenCreated;
    public UUID    projectId;
    public String  projectName;
    public UUID    userId;
    public String  userUsername;
    public String  userName;
    public String  role;
}
