package org.smartvert.smartvert.model.dto;

import jakarta.validation.constraints.Size;

public record UpdateSavedCalculationRequest(
    @Size(max = 200, message = "Label must not exceed 200 characters")
    String label
) {}
