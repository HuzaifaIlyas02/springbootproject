package com.huzaifaproject.orderservice.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huzaifaproject.orderservice.exception.OrderProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;

/**
 * Extracts the username from a Keycloak-issued JWT token present in the current request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUsernameResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;

    public String resolveUsername(HttpServletRequest request) {
        if (request == null) {
            throw new OrderProcessingException("Request context is not available. Unable to resolve user information");
        }

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new OrderProcessingException("Authorization header is missing or invalid");
        }

        return extractUsername(authorizationHeader.substring(BEARER_PREFIX.length()));
    }

    public String extractUsername(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length < 2) {
                throw new OrderProcessingException("JWT token structure is invalid");
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(tokenParts[1]);
            JsonNode payload = objectMapper.readTree(decodedPayload);

            if (payload.hasNonNull("preferred_username")) {
                String resolved = payload.get("preferred_username").asText();
                log.debug("Resolved preferred_username claim: {}", resolved);
                return resolved;
            }
            if (payload.hasNonNull("email")) {
                String resolved = payload.get("email").asText();
                log.debug("Resolved email claim as username: {}", resolved);
                return resolved;
            }
            if (payload.hasNonNull("sub")) {
                String resolved = payload.get("sub").asText();
                log.debug("Resolved sub claim as username: {}", resolved);
                return resolved;
            }

            throw new OrderProcessingException("Username claim not found in JWT token");
        } catch (IllegalArgumentException | IOException ex) {
            log.error("Failed to parse JWT token: {}", ex.getMessage());
            throw new OrderProcessingException("Unable to parse JWT token");
        }
    }
}
