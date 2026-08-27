package org.smartvert.smartvert.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.smartvert.smartvert.model.entity.UsageMode;
import java.math.BigDecimal;
import java.util.List;

public record CalculationRequest(
    @NotNull(message = "Usage mode is required")
    UsageMode usageMode,

    @DecimalMin(value = "0.0", message = "Backup hours cannot be negative")
    @DecimalMax(value = "72.0", message = "Backup hours cannot exceed 72.0")
    BigDecimal backupHours,

    @NotEmpty(message = "At least one load item must be supplied")
    List<@Valid LoadItemRequest> items
) {}
