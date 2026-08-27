package org.smartvert.smartvert.service.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.smartvert.smartvert.model.entity.UsageMode;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculatorTests {

    private final LoadCalculator loadCalculator = new LoadCalculator();
    private final SurgeCalculator surgeCalculator = new SurgeCalculator();
    private final EnergyCalculator energyCalculator = new EnergyCalculator();
    private final InverterCalculator inverterCalculator = new InverterCalculator();
    private final BatteryCalculator batteryCalculator = new BatteryCalculator();
    private final SolarCalculator solarCalculator = new SolarCalculator();

    @Test
    void testLoadCalculator() {
        CalculatorInputItem item1 = new CalculatorInputItem("TV", 2, new BigDecimal("80.00"), new BigDecimal("6.0"), false, BigDecimal.ONE);
        CalculatorInputItem item2 = new CalculatorInputItem("Fridge", 1, new BigDecimal("250.00"), new BigDecimal("8.0"), true, new BigDecimal("4.0"));

        BigDecimal runningLoad = loadCalculator.calculateRunningLoad(List.of(item1, item2));
        assertEquals(0, new BigDecimal("410.00").compareTo(runningLoad));
    }

    @ParameterizedTest
    @CsvSource({
        "100.00, 1, 100.00",
        "250.00, 2, 500.00",
        "1500.00, 3, 4500.00"
    })
    void shouldCalculateRunningLoadCorrectly(BigDecimal wattage, int quantity, BigDecimal expected) {
        CalculatorInputItem item = new CalculatorInputItem("Item", quantity, wattage, BigDecimal.ONE, false, BigDecimal.ONE);
        BigDecimal result = loadCalculator.calculateRunningLoad(List.of(item));
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    void testSurgeCalculator() {
        CalculatorInputItem item1 = new CalculatorInputItem("TV", 2, new BigDecimal("80.00"), new BigDecimal("6.0"), false, BigDecimal.ONE);
        CalculatorInputItem item2 = new CalculatorInputItem("Fridge", 1, new BigDecimal("250.00"), new BigDecimal("8.0"), true, new BigDecimal("4.00"));
        CalculatorInputItem item3 = new CalculatorInputItem("AC", 1, new BigDecimal("800.00"), new BigDecimal("5.0"), true, new BigDecimal("3.00"));

        BigDecimal runningLoad = loadCalculator.calculateRunningLoad(List.of(item1, item2, item3));
        BigDecimal peakLoad = surgeCalculator.calculatePeakLoad(runningLoad, List.of(item1, item2, item3));
        assertEquals(0, new BigDecimal("2810.00").compareTo(peakLoad));
    }

    @Test
    void testEnergyCalculator() {
        CalculatorInputItem item1 = new CalculatorInputItem("TV", 2, new BigDecimal("80.00"), new BigDecimal("6.0"), false, BigDecimal.ONE);
        CalculatorInputItem item2 = new CalculatorInputItem("Fridge", 1, new BigDecimal("250.00"), new BigDecimal("8.0"), true, new BigDecimal("4.00"));

        BigDecimal dailyEnergy = energyCalculator.calculateDailyEnergy(List.of(item1, item2));
        assertEquals(0, new BigDecimal("2960.00").compareTo(dailyEnergy));
    }

    @Test
    void testInverterCalculator() {
        BigDecimal peakLoad = new BigDecimal("1060.00");
        BigDecimal safetyMargin = new BigDecimal("0.20");
        BigDecimal powerFactor = new BigDecimal("0.80");

        BigDecimal requiredKva = inverterCalculator.calculateRequiredKva(peakLoad, safetyMargin, powerFactor);
        assertEquals(0, new BigDecimal("1.590000").compareTo(requiredKva));

        List<BigDecimal> tiers = List.of(
                new BigDecimal("1.00"),
                new BigDecimal("1.50"),
                new BigDecimal("2.50"),
                new BigDecimal("3.50"),
                new BigDecimal("5.00")
        );

        BigDecimal recommended = inverterCalculator.recommendInverterKva(requiredKva, tiers);
        assertEquals(0, new BigDecimal("2.50").compareTo(recommended));
    }

    @Test
    void testBatteryCalculatorBackupMode() {
        BigDecimal runningLoad = new BigDecimal("310.00");
        BigDecimal backupHours = new BigDecimal("8.00");
        BigDecimal dailyEnergyWh = new BigDecimal("2720.00");
        BigDecimal inverterEfficiency = new BigDecimal("0.90");
        BigDecimal batteryEfficiency = new BigDecimal("0.85");
        Integer systemVoltage = 24;
        BigDecimal batteryDod = new BigDecimal("0.50");

        BatteryCalculator.BatteryResult result = batteryCalculator.calculateBatteryRequirements(
                UsageMode.BACKUP, runningLoad, backupHours, dailyEnergyWh,
                inverterEfficiency, batteryEfficiency, systemVoltage, batteryDod
        );

        assertEquals(0, new BigDecimal("270.152505").compareTo(result.capacityAh()));
        assertEquals(0, new BigDecimal("3.241830").compareTo(result.energyKwh()));
    }

    @Test
    void testSolarCalculator() {
        BigDecimal dailyEnergyWh = new BigDecimal("2720.00");
        BigDecimal peakSunHours = new BigDecimal("4.50");
        BigDecimal solarEfficiency = new BigDecimal("0.75");
        BigDecimal defaultPanelWattage = new BigDecimal("450.00");

        SolarCalculator.SolarResult result = solarCalculator.calculateSolarRequirements(
                dailyEnergyWh, peakSunHours, solarEfficiency, defaultPanelWattage
        );

        assertEquals(0, new BigDecimal("0.805926").compareTo(result.capacityKw()));
        assertEquals(2, result.panelCount());
    }
}
