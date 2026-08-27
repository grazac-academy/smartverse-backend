package org.smartvert.smartvert.service.engine;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class LoadCalculator {

    public BigDecimal calculateRunningLoad(List<CalculatorInputItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(item -> item.wattage().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
