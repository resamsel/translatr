package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalFeatureFlagDto {
    public UUID    id;
    public Instant whenCreated;
    public String  feature;
    public boolean enabled;
}
