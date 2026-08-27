package org.smartvert.smartvert.service.engine;

import java.math.BigDecimal;

public record CalculatorInputItem(
    String applianceName,
    int quantity,
    BigDecimal wattage,
    BigDecimal hoursPerDay,
    boolean surgeApplicable,
    BigDecimal surgeMultiplier
) {}
