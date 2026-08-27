package org.smartvert.smartvert.service.engine;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class SurgeCalculator {

    public BigDecimal calculatePeakLoad(BigDecimal runningLoad, List<CalculatorInputItem> items) {
        if (items == null || items.isEmpty()) {
            return runningLoad;
        }

        BigDecimal maxSurgeDelta = BigDecimal.ZERO;

        for (CalculatorInputItem item : items) {
            if (item.surgeApplicable() && item.surgeMultiplier().compareTo(BigDecimal.ONE) > 0) {
                // delta = (wattage * multiplier) - wattage
                BigDecimal surgeWatts = item.wattage().multiply(item.surgeMultiplier());
                BigDecimal delta = surgeWatts.subtract(item.wattage());
                if (delta.compareTo(maxSurgeDelta) > 0) {
                    maxSurgeDelta = delta;
                }
            }
        }

        return runningLoad.add(maxSurgeDelta);
    }
}
