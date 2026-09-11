package org.smartvert.smartvert.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SaveCalculationRequest(
    @NotNull(message = "Calculation ID is required")
    UUID calculationId,

    @Size(max = 200, message = "Label must not exceed 200 characters")
    String label
) {}
