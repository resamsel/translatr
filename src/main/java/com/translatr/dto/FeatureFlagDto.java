package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureFlagDto {
    public UUID    id;
    public Instant whenCreated;
    public UUID    userId;
    public String  feature;
    public boolean enabled;
}
