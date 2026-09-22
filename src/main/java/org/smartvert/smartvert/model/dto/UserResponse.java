package org.smartvert.smartvert.model.dto;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    String userType,
    String state,
    String phoneNumber,
    boolean isEmailVerified
) {}

