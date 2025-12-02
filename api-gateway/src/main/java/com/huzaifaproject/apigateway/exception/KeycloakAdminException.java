package com.huzaifaproject.apigateway.exception;

import org.springframework.http.HttpStatus;

public class KeycloakAdminException extends RuntimeException {

    private final HttpStatus status;

    public KeycloakAdminException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
