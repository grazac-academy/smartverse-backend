package org.smartvert.smartvert.service.engine;

import org.smartvert.smartvert.model.entity.UsageMode;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BatteryCalculator {

    public BatteryResult calculateBatteryRequirements(
            UsageMode usageMode,
            BigDecimal runningLoad,
            BigDecimal backupHours,
            BigDecimal dailyEnergyWh,
            BigDecimal inverterEfficiency,
            BigDecimal batteryEfficiency,
            Integer systemVoltage,
            BigDecimal batteryDod) {

        if (runningLoad.compareTo(BigDecimal.ZERO) <= 0 && dailyEnergyWh.compareTo(BigDecimal.ZERO) <= 0) {
            return new BatteryResult(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal eReq;
        if (usageMode == UsageMode.BACKUP) {
            BigDecimal hours = (backupHours != null) ? backupHours : BigDecimal.ONE;
            eReq = runningLoad.multiply(hours);
        } else {
            eReq = dailyEnergyWh;
        }

        // E_gross = E_req / (inverterEff * batteryEff)
        BigDecimal effMultiplier = inverterEfficiency.multiply(batteryEfficiency);
        if (effMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            effMultiplier = BigDecimal.ONE;
        }
        BigDecimal eGross = eReq.divide(effMultiplier, 6, RoundingMode.HALF_UP);

        // Ah = E_gross / (systemVoltage * DoD)
        BigDecimal voltage = BigDecimal.valueOf(systemVoltage != null ? systemVoltage : 12);
        BigDecimal divisor = voltage.multiply(batteryDod);
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            divisor = BigDecimal.ONE;
        }
        BigDecimal capacityAh = eGross.divide(divisor, 6, RoundingMode.HALF_UP);

        // energyKwh = E_gross / 1000
        BigDecimal energyKwh = eGross.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        return new BatteryResult(capacityAh, energyKwh);
    }

    public record BatteryResult(BigDecimal capacityAh, BigDecimal energyKwh) {}
}
