package org.smartvert.smartvert.model.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String email,
    String fullName,
    boolean isEmailVerified
) {}


