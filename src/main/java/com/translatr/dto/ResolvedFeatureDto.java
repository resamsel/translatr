package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/**
 * Per-feature resolution detail for the admin UI: the hardcoded default, the stored global
 * setting (or null), the caller's own override (value + row id, or null) and the resulting
 * effective value.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResolvedFeatureDto {
    public String  feature;
    public boolean defaultEnabled;
    public Boolean global;
    public Boolean userOverride;
    public UUID    userOverrideId;
    public boolean effective;
}
