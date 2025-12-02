package com.huzaifaproject.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huzaifaproject.apigateway.config.KeycloakAdminProperties;
import com.huzaifaproject.apigateway.dto.SignupRequest;
import com.huzaifaproject.apigateway.dto.SignupResult;
import com.huzaifaproject.apigateway.exception.KeycloakAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class KeycloakAdminService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminService.class);

    private final KeycloakAdminProperties props;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KeycloakAdminService(KeycloakAdminProperties props,
                                ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        
        // Create RestTemplate manually since RestTemplateBuilder isn't available in WebFlux
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    public SignupResult createUser(SignupRequest request) {
        String email = normalizeEmail(request.email());
        String username = resolveUsername(request.username(), email);
        String token = obtainAdminAccessToken();

        String userId = createUserEntity(token, username, email, request.firstName(), request.lastName());
        resetPassword(token, userId, request.password());
        assignRole(token, userId, props.getDefaultRole());

        log.info("Created Keycloak user {} at {}", username, Instant.now());
        return new SignupResult(userId, username, email);
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new KeycloakAdminException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveUsername(String providedUsername, String fallbackEmail) {
        if (StringUtils.hasText(providedUsername)) {
            return providedUsername.trim();
        }
        return fallbackEmail;
    }

    private String obtainAdminAccessToken() {
        String serverUrl = require(props.getServerUrl(), "keycloak.admin.server-url");
        String clientId = require(props.getClientId(), "keycloak.admin.client-id");
        String clientSecret = require(props.getClientSecret(), "keycloak.admin.client-secret");
        String realmSegment = Optional.ofNullable(props.getTokenRealm()).filter(StringUtils::hasText).orElse("master");

        String tokenUrl = serverUrl + "/realms/" + realmSegment + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(form, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Keycloak rejected admin token request");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String accessToken = root.path("access_token").asText(null);
            if (!StringUtils.hasText(accessToken)) {
                throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Keycloak token response was missing access_token");
            }
            return accessToken;
        } catch (HttpStatusCodeException ex) {
            log.error("Failed to obtain Keycloak admin token: {}", ex.getResponseBodyAsString());
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Unable to obtain admin token from Keycloak");
        } catch (JsonProcessingException ex) {
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Unable to parse Keycloak token response");
        }
    }

    private String createUserEntity(String token,
                                    String username,
                                    String email,
                                    String firstName,
                                    String lastName) {
        String realm = require(props.getRealm(), "keycloak.admin.realm");
        String usersUrl = require(props.getServerUrl(), "keycloak.admin.server-url") + "/admin/realms/" + realm + "/users";

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("username", username);
        payload.put("email", email);
        payload.put("enabled", true);
        payload.put("emailVerified", false);
        if (StringUtils.hasText(firstName)) {
            payload.put("firstName", firstName.trim());
        }
        if (StringUtils.hasText(lastName)) {
            payload.put("lastName", lastName.trim());
        }

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), bearerHeaders(token));

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(usersUrl, entity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return extractUserId(response);
            }
            log.error("Unexpected status from Keycloak user creation: {}", response.getStatusCode());
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Keycloak user creation failed");
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new KeycloakAdminException(HttpStatus.CONFLICT, "User already exists");
            }
            log.error("Keycloak user creation error: {}", ex.getResponseBodyAsString());
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Failed to create user in Keycloak");
        }
    }

    private void resetPassword(String token, String userId, String password) {
        String realm = require(props.getRealm(), "keycloak.admin.realm");
        String resetUrl = require(props.getServerUrl(), "keycloak.admin.server-url")
                + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

        ObjectNode credentials = objectMapper.createObjectNode();
        credentials.put("type", "password");
        credentials.put("temporary", false);
        credentials.put("value", password);

        HttpEntity<String> entity = new HttpEntity<>(credentials.toString(), bearerHeaders(token));
        try {
            restTemplate.exchange(resetUrl, HttpMethod.PUT, entity, Void.class);
        } catch (HttpStatusCodeException ex) {
            log.error("Failed to set password for user {}: {}", userId, ex.getResponseBodyAsString());
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Failed to set user password in Keycloak");
        }
    }

    private void assignRole(String token, String userId, String roleName) {
        if (!StringUtils.hasText(roleName)) {
            log.warn("Default role not configured; skipping role assignment");
            return;
        }

        JsonNode role = fetchRole(token, roleName);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        ObjectNode roleNode = objectMapper.createObjectNode();
        roleNode.put("id", role.path("id").asText());
        roleNode.put("name", role.path("name").asText());
        roleNode.put("composite", role.path("composite").asBoolean(false));
        arrayNode.add(roleNode);

        String realm = require(props.getRealm(), "keycloak.admin.realm");
        String assignUrl = require(props.getServerUrl(), "keycloak.admin.server-url")
                + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        HttpEntity<String> entity = new HttpEntity<>(arrayNode.toString(), bearerHeaders(token));

        try {
            restTemplate.postForEntity(assignUrl, entity, Void.class);
        } catch (HttpStatusCodeException ex) {
            log.error("Failed to assign role {} to user {}: {}", roleName, userId, ex.getResponseBodyAsString());
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Failed to assign role to user");
        }
    }

    private JsonNode fetchRole(String token, String roleName) {
        String realm = require(props.getRealm(), "keycloak.admin.realm");
        String roleUrl = require(props.getServerUrl(), "keycloak.admin.server-url")
                + "/admin/realms/" + realm + "/roles/" + roleName;

        HttpEntity<Void> entity = new HttpEntity<>(bearerHeaders(token));

        try {
            ResponseEntity<String> response = restTemplate.exchange(roleUrl, HttpMethod.GET, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Unable to fetch Keycloak role " + roleName);
            }
            return objectMapper.readTree(response.getBody());
        } catch (HttpStatusCodeException ex) {
            log.error("Failed to fetch role {}: {}", roleName, ex.getResponseBodyAsString());
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Keycloak role lookup failed");
        } catch (JsonProcessingException ex) {
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Unable to parse Keycloak role response");
        }
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    private String extractUserId(ResponseEntity<Void> response) {
        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (!StringUtils.hasText(location)) {
            throw new KeycloakAdminException(HttpStatus.BAD_GATEWAY, "Keycloak response missing user location header");
        }
        int idx = location.lastIndexOf('/');
        return idx >= 0 ? location.substring(idx + 1) : location;
    }

    private String require(String value, String propertyName) {
        if (!StringUtils.hasText(value)) {
            throw new KeycloakAdminException(HttpStatus.INTERNAL_SERVER_ERROR, propertyName + " is not configured");
        }
        return value;
    }
}
