package org.smartvert.smartvert.service.engine;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SolarCalculator {

    public SolarResult calculateSolarRequirements(
            BigDecimal dailyEnergyWh,
            BigDecimal peakSunHours,
            BigDecimal solarEfficiency,
            BigDecimal defaultPanelWattage) {

        if (dailyEnergyWh.compareTo(BigDecimal.ZERO) <= 0) {
            return new SolarResult(BigDecimal.ZERO, 0, defaultPanelWattage);
        }

        // P_solar_kW = E_daily / (peakSunHours * solarEfficiency * 1000)
        BigDecimal divisor = peakSunHours.multiply(solarEfficiency).multiply(BigDecimal.valueOf(1000));
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            divisor = BigDecimal.ONE;
        }
        BigDecimal capacityKw = dailyEnergyWh.divide(divisor, 6, RoundingMode.HALF_UP);

        // Panel Count = Math.ceil((capacityKw * 1000) / panelWattage)
        BigDecimal totalWattsNeeded = capacityKw.multiply(BigDecimal.valueOf(1000));
        BigDecimal panelCountFraction = totalWattsNeeded.divide(defaultPanelWattage, 6, RoundingMode.HALF_UP);
        int panelCount = panelCountFraction.setScale(0, RoundingMode.CEILING).intValue();

        return new SolarResult(capacityKw, panelCount, defaultPanelWattage);
    }

    public record SolarResult(BigDecimal capacityKw, Integer panelCount, BigDecimal panelWatts) {}
}
