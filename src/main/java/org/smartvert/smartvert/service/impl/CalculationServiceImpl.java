package org.smartvert.smartvert.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.exception.ResourceNotFoundException;
import org.smartvert.smartvert.exception.ValidationException;
import org.smartvert.smartvert.model.dto.CalculationRequest;
import org.smartvert.smartvert.model.dto.CalculationResult;
import org.smartvert.smartvert.model.dto.LoadItemRequest;
import org.smartvert.smartvert.model.entity.*;
import org.smartvert.smartvert.repository.*;
import org.smartvert.smartvert.service.CalculationService;
import org.smartvert.smartvert.service.engine.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {

    private final ApplianceRepository applianceRepository;
    private final CalculationConfigurationRepository configRepository;
    private final InverterOptionRepository inverterOptionRepository;
    private final CalculationRepository calculationRepository;

    private final LoadCalculator loadCalculator;
    private final SurgeCalculator surgeCalculator;
    private final EnergyCalculator energyCalculator;
    private final InverterCalculator inverterCalculator;
    private final BatteryCalculator batteryCalculator;
    private final SolarCalculator solarCalculator;
    private final RecommendationAssembler recommendationAssembler;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CalculationResult calculate(CalculationRequest request) {
        CalculationConfiguration config = configRepository.findFirstByActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No active calculation configuration found"));

        List<CalculatorInputItem> calculatorInputs = new ArrayList<>();

        for (LoadItemRequest itemRequest : request.items()) {
            Appliance appliance = applianceRepository.findById(itemRequest.applianceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appliance not found with id: " + itemRequest.applianceId()));

            if (!appliance.getActive()) {
                throw new ValidationException("Appliance is inactive: " + appliance.getName());
            }

            BigDecimal wattage = itemRequest.wattage();
            if (wattage.compareTo(appliance.getMinWattage()) < 0 || wattage.compareTo(appliance.getMaxWattage()) > 0) {
                throw new ValidationException(String.format(
                        "Wattage override for %s must be between %sW and %sW",
                        appliance.getName(), appliance.getMinWattage(), appliance.getMaxWattage()
                ));
            }

            calculatorInputs.add(new CalculatorInputItem(
                    appliance.getName(),
                    itemRequest.quantity(),
                    wattage,
                    itemRequest.hoursPerDay(),
                    appliance.getSurgeApplicable(),
                    appliance.getSurgeMultiplier()
            ));
        }

        BigDecimal runningLoad = loadCalculator.calculateRunningLoad(calculatorInputs);
        BigDecimal peakLoad = surgeCalculator.calculatePeakLoad(runningLoad, calculatorInputs);
        BigDecimal dailyEnergyWh = energyCalculator.calculateDailyEnergy(calculatorInputs);

        BigDecimal requiredKva = inverterCalculator.calculateRequiredKva(
                peakLoad, config.getInverterSafetyMargin(), config.getPowerFactor()
        );

        List<InverterOption> inverterOptions = inverterOptionRepository.findByActiveTrueOrderByRatingKvaAsc();
        List<BigDecimal> inverterRatings = inverterOptions.stream()
                .map(InverterOption::getRatingKva)
                .collect(Collectors.toList());

        BigDecimal recommendedKva = inverterCalculator.recommendInverterKva(requiredKva, inverterRatings);

        Integer systemVoltage = inverterOptions.stream()
                .filter(opt -> opt.getRatingKva().compareTo(recommendedKva) == 0)
                .map(InverterOption::getSystemVoltage)
                .findFirst()
                .orElse(24);

        BatteryCalculator.BatteryResult batteryResult = batteryCalculator.calculateBatteryRequirements(
                request.usageMode(),
                runningLoad,
                request.backupHours(),
                dailyEnergyWh,
                config.getInverterEfficiency(),
                config.getBatteryEfficiency(),
                systemVoltage,
                config.getBatteryDod()
        );

        SolarCalculator.SolarResult solarResult = solarCalculator.calculateSolarRequirements(
                dailyEnergyWh,
                config.getPeakSunHours(),
                config.getSolarEfficiency(),
                config.getDefaultPanelWattage()
        );

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            payloadJson = "{}";
        }

        if (runningLoad.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ValidationException("Calculated running load exceeds maximum supported limit (99,999,999.99 W)");
        }
        if (peakLoad.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ValidationException("Calculated peak surge load exceeds maximum supported limit (99,999,999.99 W)");
        }
        if (dailyEnergyWh.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new ValidationException("Calculated daily energy requirement exceeds maximum supported limit (9,999,999,999.99 Wh)");
        }
        if (recommendedKva.compareTo(new BigDecimal("9999.99")) > 0) {
            throw new ValidationException("Calculated recommended inverter rating exceeds maximum supported limit (9,999.99 kVA)");
        }
        if (batteryResult.capacityAh().compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ValidationException("Calculated recommended battery capacity exceeds maximum supported limit (99,999,999.99 Ah)");
        }
        if (solarResult.capacityKw().compareTo(new BigDecimal("99999.999")) > 0) {
            throw new ValidationException("Calculated recommended solar capacity exceeds maximum supported limit (99,999.999 kW)");
        }

        Calculation calculation = Calculation.builder()
                .configuration(config)
                .usageMode(request.usageMode().name())
                .backupHours(request.backupHours())
                .totalRunningWatts(runningLoad)
                .peakSurgeWatts(peakLoad)
                .dailyEnergyWh(dailyEnergyWh)
                .recommendedInverterKva(recommendedKva)
                .recommendedBatteryAh(batteryResult.capacityAh())
                .recommendedBatteryKwh(batteryResult.energyKwh())
                .recommendedSolarKw(solarResult.capacityKw())
                .panelCount(solarResult.panelCount())
                .inputPayload(payloadJson)
                .build();

        Calculation saved = calculationRepository.save(calculation);

        return recommendationAssembler.assembleResult(
                saved.getId(),
                config.getVersion(),
                runningLoad,
                peakLoad,
                dailyEnergyWh,
                recommendedKva,
                systemVoltage,
                batteryResult.capacityAh(),
                batteryResult.energyKwh(),
                config.getBatteryDod(),
                solarResult.capacityKw(),
                solarResult.panelCount(),
                solarResult.panelWatts(),
                calculatorInputs
        );
    }
}
