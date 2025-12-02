package com.huzaifaproject.apigateway.controller;

import com.huzaifaproject.apigateway.dto.ApiErrorResponse;
import com.huzaifaproject.apigateway.exception.KeycloakAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(KeycloakAdminException.class)
    public ResponseEntity<ApiErrorResponse> handleKeycloak(KeycloakAdminException ex, ServerWebExchange exchange) {
        log.warn("Keycloak admin error: {}", ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getMessage(), exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
        String message = ex.getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", exchange);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, ServerWebExchange exchange) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange != null ? exchange.getRequest().getPath().value() : null
        );
        return ResponseEntity.status(status).body(response);
    }
}
