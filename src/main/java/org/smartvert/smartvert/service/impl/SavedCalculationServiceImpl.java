package org.smartvert.smartvert.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smartvert.smartvert.exception.DuplicateResourceException;
import org.smartvert.smartvert.exception.ResourceNotFoundException;
import org.smartvert.smartvert.model.dto.*;
import org.smartvert.smartvert.model.entity.AppUser;
import org.smartvert.smartvert.model.entity.Appliance;
import org.smartvert.smartvert.model.entity.Calculation;
import org.smartvert.smartvert.model.entity.InverterOption;
import org.smartvert.smartvert.model.entity.SavedCalculation;
import org.smartvert.smartvert.repository.AppUserRepository;
import org.smartvert.smartvert.repository.ApplianceRepository;
import org.smartvert.smartvert.repository.CalculationRepository;
import org.smartvert.smartvert.repository.InverterOptionRepository;
import org.smartvert.smartvert.repository.SavedCalculationRepository;
import org.smartvert.smartvert.service.SavedCalculationService;
import org.smartvert.smartvert.service.engine.CalculatorInputItem;
import org.smartvert.smartvert.service.engine.RecommendationAssembler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedCalculationServiceImpl implements SavedCalculationService {

    private final SavedCalculationRepository savedCalculationRepository;
    private final CalculationRepository calculationRepository;
    private final AppUserRepository appUserRepository;
    private final ApplianceRepository applianceRepository;
    private final InverterOptionRepository inverterOptionRepository;
    private final RecommendationAssembler recommendationAssembler;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SavedCalculationDTO save(SaveCalculationRequest request) {
        AppUser user = getCurrentAuthenticatedUser();

        if (savedCalculationRepository.existsByUserIdAndCalculationId(user.getId(), request.calculationId())) {
            throw new DuplicateResourceException("This calculation is already saved to your profile");
        }

        Calculation calculation = calculationRepository.findById(request.calculationId())
                .orElseThrow(() -> new ResourceNotFoundException("Calculation not found"));

        SavedCalculation saved = SavedCalculation.builder()
                .user(user)
                .calculation(calculation)
                .label(request.label())
                .build();

        SavedCalculation persisted = savedCalculationRepository.save(saved);
        return toDTO(persisted);
    }

    @Override
    public List<SavedCalculationDTO> getAllByUser() {
        AppUser user = getCurrentAuthenticatedUser();
        return savedCalculationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SavedCalculationDTO getById(UUID savedCalculationId) {
        AppUser user = getCurrentAuthenticatedUser();
        SavedCalculation saved = savedCalculationRepository.findByIdAndUserId(savedCalculationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Saved calculation not found"));
        return toDTO(saved);
    }

    @Override
    @Transactional
    public SavedCalculationDTO updateLabel(UUID savedCalculationId, UpdateSavedCalculationRequest request) {
        AppUser user = getCurrentAuthenticatedUser();
        SavedCalculation saved = savedCalculationRepository.findByIdAndUserId(savedCalculationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Saved calculation not found"));

        saved.setLabel(request.label());
        SavedCalculation updated = savedCalculationRepository.save(saved);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public void delete(UUID savedCalculationId) {
        AppUser user = getCurrentAuthenticatedUser();
        SavedCalculation saved = savedCalculationRepository.findByIdAndUserId(savedCalculationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Saved calculation not found"));
        savedCalculationRepository.delete(saved);
    }

    private AppUser getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResourceNotFoundException("User not found");
        }
        String email = auth.getName();
        return appUserRepository.findByEmailAndDeletedFalse(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SavedCalculationDTO toDTO(SavedCalculation saved) {
        Calculation calc = saved.getCalculation();

        Integer systemVoltage = resolveSystemVoltage(calc.getRecommendedInverterKva());
        BigDecimal dodRatio = calc.getConfiguration() != null ? calc.getConfiguration().getBatteryDod() : null;
        BigDecimal defaultPanelWattage = calc.getConfiguration() != null ? calc.getConfiguration().getDefaultPanelWattage() : null;
        String version = calc.getConfiguration() != null ? calc.getConfiguration().getVersion() : null;

        List<CalculatorInputItem> calculatorInputs = extractCalculatorInputs(calc.getInputPayload());

        CalculationResult result = recommendationAssembler.assembleResult(
                calc.getId(),
                version,
                calc.getTotalRunningWatts(),
                calc.getPeakSurgeWatts(),
                calc.getDailyEnergyWh(),
                calc.getRecommendedInverterKva(),
                systemVoltage,
                calc.getRecommendedBatteryAh(),
                calc.getRecommendedBatteryKwh(),
                dodRatio,
                calc.getRecommendedSolarKw(),
                calc.getPanelCount(),
                defaultPanelWattage,
                calculatorInputs
        );

        return new SavedCalculationDTO(
                saved.getId(),
                calc.getId(),
                saved.getLabel(),
                saved.getCreatedAt(),
                result
        );
    }

    private Integer resolveSystemVoltage(BigDecimal recommendedKva) {
        if (recommendedKva == null) {
            return 24;
        }
        return inverterOptionRepository.findAll().stream()
                .filter(opt -> opt.getRatingKva() != null && opt.getRatingKva().compareTo(recommendedKva) == 0)
                .map(InverterOption::getSystemVoltage)
                .findFirst()
                .orElse(24);
    }

    private List<CalculatorInputItem> extractCalculatorInputs(String inputPayload) {
        if (inputPayload == null || inputPayload.isBlank()) {
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(inputPayload);
            JsonNode itemsNode = root.get("items");
            if (itemsNode == null || !itemsNode.isArray() || itemsNode.isEmpty()) {
                return Collections.emptyList();
            }

            List<LoadItemRecord> items = new ArrayList<>();
            Set<UUID> applianceIds = new HashSet<>();

            for (JsonNode itemNode : itemsNode) {
                if (itemNode.has("applianceId")) {
                    UUID applianceId = UUID.fromString(itemNode.get("applianceId").asText());
                    int quantity = itemNode.has("quantity") ? itemNode.get("quantity").asInt(1) : 1;
                    BigDecimal wattage = itemNode.has("wattage") ? new BigDecimal(itemNode.get("wattage").asText()) : BigDecimal.ZERO;
                    BigDecimal hoursPerDay = itemNode.has("hoursPerDay") ? new BigDecimal(itemNode.get("hoursPerDay").asText()) : BigDecimal.ZERO;
                    items.add(new LoadItemRecord(applianceId, quantity, wattage, hoursPerDay));
                    applianceIds.add(applianceId);
                }
            }

            Map<UUID, Appliance> applianceMap = applianceRepository.findAllById(applianceIds).stream()
                    .collect(Collectors.toMap(Appliance::getId, Function.identity()));

            List<CalculatorInputItem> calculatorInputs = new ArrayList<>();
            for (LoadItemRecord item : items) {
                Appliance appliance = applianceMap.get(item.applianceId());
                String name = appliance != null ? appliance.getName() : "Unknown Appliance";
                boolean surgeApplicable = appliance != null && Boolean.TRUE.equals(appliance.getSurgeApplicable());
                BigDecimal surgeMultiplier = (appliance != null && appliance.getSurgeMultiplier() != null)
                        ? appliance.getSurgeMultiplier()
                        : BigDecimal.ONE;

                calculatorInputs.add(new CalculatorInputItem(
                        name,
                        item.quantity(),
                        item.wattage(),
                        item.hoursPerDay(),
                        surgeApplicable,
                        surgeMultiplier
                ));
            }

            return calculatorInputs;
        } catch (Exception e) {
            log.warn("Failed to parse calculation input payload: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private record LoadItemRecord(UUID applianceId, int quantity, BigDecimal wattage, BigDecimal hoursPerDay) {}
}
