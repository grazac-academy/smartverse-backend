package org.smartvert.smartvert.model.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
    @NotBlank(message = "Token is required")
    String token
) {}
