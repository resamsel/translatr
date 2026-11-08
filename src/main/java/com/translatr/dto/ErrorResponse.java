package com.translatr.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Without this, the native image build strips the reflective metadata Jackson
 * needs to serialize this DTO's public fields — every mapper in
 * {@link com.translatr.filter.ExceptionMappers} that returns one then 500s
 * with "No serializer found for class ... this appears to be a native image"
 * instead of the intended status code.
 */
@RegisterForReflection
public class ErrorResponse {
    public int    status;
    public String message;

    public ErrorResponse(int status, String message) {
        this.status  = status;
        this.message = message;
    }
}
