package org.smartvert.smartvert.model.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    boolean isEmailVerified
) {}


