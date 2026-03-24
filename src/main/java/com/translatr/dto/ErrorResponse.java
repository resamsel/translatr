package com.translatr.dto;

public class ErrorResponse {
    public int    status;
    public String message;

    public ErrorResponse(int status, String message) {
        this.status  = status;
        this.message = message;
    }
}
