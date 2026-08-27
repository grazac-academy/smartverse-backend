package org.smartvert.smartvert.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplianceDTO(
    UUID id,
    UUID categoryId,
    String code,
    String name,
    BigDecimal defaultWattage,
    BigDecimal minWattage,
    BigDecimal maxWattage,
    Integer defaultVoltage,
    Boolean surgeApplicable,
    BigDecimal surgeMultiplier,
    Boolean heavyLoad,
    Boolean active
) {}
