package org.smartvert.smartvert.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CalculationResult(
    UUID calculationId,
    String calculationVersion,
    Summary summary,
    Recommendation recommendation,
    List<BreakdownItem> breakdown
) {
    public record Summary(
        BigDecimal totalRunningLoadWatts,
        BigDecimal peakLoadWatts,
        BigDecimal dailyEnergyWh
    ) {}

    public record Recommendation(
        BigDecimal inverterKva,
        BatteryRecommendation battery,
        SolarRecommendation solar
    ) {}

    public record BatteryRecommendation(
        Integer systemVoltage,
        BigDecimal capacityAh,
        BigDecimal energyKwh,
        BigDecimal dodPercentage
    ) {}

    public record SolarRecommendation(
        BigDecimal capacityKw,
        Integer panelCount,
        BigDecimal panelWatts
    ) {}

    public record BreakdownItem(
        String applianceName,
        Integer quantity,
        BigDecimal wattage,
        BigDecimal runningWatts,
        BigDecimal hoursPerDay,
        BigDecimal dailyEnergyWh,
        BigDecimal surgeWatts,
        Boolean surgeApplicable
    ) {}
}
