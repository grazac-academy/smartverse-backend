package org.smartvert.smartvert.service.engine;

import org.smartvert.smartvert.model.dto.CalculationResult;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RecommendationAssembler {

    public CalculationResult assembleResult(
            UUID calculationId,
            String configVersion,
            BigDecimal runningLoad,
            BigDecimal peakLoad,
            BigDecimal dailyEnergyWh,
            BigDecimal recommendedInverterKva,
            Integer systemVoltage,
            BigDecimal capacityAh,
            BigDecimal batteryEnergyKwh,
            BigDecimal dodPercentage,
            BigDecimal solarCapacityKw,
            Integer solarPanelCount,
            BigDecimal panelWatts,
            List<CalculatorInputItem> items) {

        CalculationResult.Summary summary = new CalculationResult.Summary(
                runningLoad.setScale(2, RoundingMode.HALF_UP),
                peakLoad.setScale(2, RoundingMode.HALF_UP),
                dailyEnergyWh.setScale(2, RoundingMode.HALF_UP)
        );

        CalculationResult.BatteryRecommendation battery = new CalculationResult.BatteryRecommendation(
                systemVoltage,
                capacityAh.setScale(2, RoundingMode.HALF_UP),
                batteryEnergyKwh.setScale(2, RoundingMode.HALF_UP),
                dodPercentage.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
        );

        CalculationResult.SolarRecommendation solar = new CalculationResult.SolarRecommendation(
                solarCapacityKw.setScale(2, RoundingMode.HALF_UP),
                solarPanelCount,
                panelWatts.setScale(2, RoundingMode.HALF_UP)
        );

        CalculationResult.Recommendation recommendation = new CalculationResult.Recommendation(
                recommendedInverterKva.setScale(2, RoundingMode.HALF_UP),
                battery,
                solar
        );

        List<CalculationResult.BreakdownItem> breakdown = items.stream()
                .map(item -> {
                    BigDecimal runningWatts = item.wattage().multiply(BigDecimal.valueOf(item.quantity()));
                    BigDecimal dailyEnergy = runningWatts.multiply(item.hoursPerDay());
                    BigDecimal surgeWatts = item.surgeApplicable()
                            ? item.wattage().multiply(item.surgeMultiplier())
                            : item.wattage();

                    return new CalculationResult.BreakdownItem(
                            item.applianceName(),
                            item.quantity(),
                            item.wattage().setScale(2, RoundingMode.HALF_UP),
                            runningWatts.setScale(2, RoundingMode.HALF_UP),
                            item.hoursPerDay().setScale(2, RoundingMode.HALF_UP),
                            dailyEnergy.setScale(2, RoundingMode.HALF_UP),
                            surgeWatts.setScale(2, RoundingMode.HALF_UP),
                            item.surgeApplicable()
                    );
                })
                .collect(Collectors.toList());

        return new CalculationResult(
                calculationId,
                configVersion,
                summary,
                recommendation,
                breakdown
        );
    }
}
