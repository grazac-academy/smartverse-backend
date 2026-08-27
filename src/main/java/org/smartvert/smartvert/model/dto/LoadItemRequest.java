package org.smartvert.smartvert.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record LoadItemRequest(
    @NotNull(message = "Appliance ID is required")
    UUID applianceId,

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    @Max(value = 1000, message = "Quantity cannot exceed 1000")
    Integer quantity,

    @NotNull(message = "Wattage is required")
    @Positive(message = "Wattage must be greater than 0")
    @DecimalMax(value = "50000.00", message = "Wattage cannot exceed 50000.00")
    BigDecimal wattage,

    @NotNull(message = "Hours per day is required")
    @DecimalMin(value = "0.0", message = "Hours per day cannot be negative")
    @DecimalMax(value = "24.0", message = "Hours per day cannot exceed 24")
    BigDecimal hoursPerDay
) {}
