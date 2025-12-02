package com.huzaifaproject.apigateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Size(max = 64, message = "Username must be at most 64 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        String password,

        @Size(max = 40, message = "First name must be at most 40 characters")
        String firstName,

        @Size(max = 40, message = "Last name must be at most 40 characters")
        String lastName
) {}
