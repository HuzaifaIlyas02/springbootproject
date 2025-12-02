package com.huzaifaproject.apigateway.controller;

import com.huzaifaproject.apigateway.dto.SignupRequest;
import com.huzaifaproject.apigateway.dto.SignupResponse;
import com.huzaifaproject.apigateway.dto.SignupResult;
import com.huzaifaproject.apigateway.service.KeycloakAdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SignupController {

    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    private final KeycloakAdminService keycloakAdminService;

    public SignupController(KeycloakAdminService keycloakAdminService) {
        this.keycloakAdminService = keycloakAdminService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for email={}", request.email());
        SignupResult result = keycloakAdminService.createUser(request);
        SignupResponse response = new SignupResponse(result.userId(), "created", "Account created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
