package com.translatr.auth;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/** 403 body for an access token lacking scopes; registered for reflection like {@code ErrorResponse}. */
@RegisterForReflection
public class InsufficientScopeResponse {
    public String       error = "insufficient_scope";
    public List<String> required;

    public InsufficientScopeResponse(List<String> required) {
        this.required = required;
    }
}
