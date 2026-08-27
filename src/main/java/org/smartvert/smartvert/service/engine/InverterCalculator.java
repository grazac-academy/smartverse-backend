package org.smartvert.smartvert.service.engine;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class InverterCalculator {

    public BigDecimal calculateRequiredKva(BigDecimal peakLoad, BigDecimal safetyMargin, BigDecimal powerFactor) {
        if (peakLoad.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // kVA_raw = (peakLoad * (1 + safetyMargin)) / (powerFactor * 1000)
        BigDecimal multiplier = BigDecimal.ONE.add(safetyMargin);
        BigDecimal dividend = peakLoad.multiply(multiplier);
        BigDecimal divisor = powerFactor.multiply(BigDecimal.valueOf(1000));
        return dividend.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    public BigDecimal recommendInverterKva(BigDecimal requiredKva, List<BigDecimal> availableTiers) {
        if (requiredKva.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (availableTiers == null || availableTiers.isEmpty()) {
            // Fallback: round up to nearest 0.5 kVA
            return requiredKva.multiply(BigDecimal.valueOf(2))
                    .setScale(0, RoundingMode.CEILING)
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }

        return availableTiers.stream()
                .filter(tier -> tier.compareTo(requiredKva) >= 0)
                .findFirst()
                .orElse(availableTiers.get(availableTiers.size() - 1)); // Return the largest if none match
    }
}
