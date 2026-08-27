package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    public UUID   id;
    public Instant whenCreated;
    public Instant whenUpdated;
    public String name;
    public String username;
    public String email;
    public String role;
    public String preferredLocale;
    public Map<String, String> settings;
    public Map<String, Boolean> features;
}
