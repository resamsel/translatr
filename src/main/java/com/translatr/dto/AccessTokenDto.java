package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessTokenDto {
    public Long    id;
    public Instant whenCreated;
    public Instant whenUpdated;
    public UUID    userId;
    public String  userUsername;
    public String  name;
    public String  key;
    public String  scope;
}
