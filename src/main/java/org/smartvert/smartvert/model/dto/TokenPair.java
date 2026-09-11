package org.smartvert.smartvert.model.dto;

public record TokenPair(
    String accessToken,
    String refreshToken
) {}
