package org.smartvert.smartvert.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SavedCalculationDTO(
    UUID id,
    UUID calculationId,
    String label,
    OffsetDateTime createdAt,
    CalculationResult calculationResult
) {}
