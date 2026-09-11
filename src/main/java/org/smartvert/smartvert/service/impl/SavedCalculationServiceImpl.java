package org.smartvert.smartvert.service.impl;

import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.exception.ResourceNotFoundException;
import org.smartvert.smartvert.model.dto.*;
import org.smartvert.smartvert.model.entity.AppUser;
import org.smartvert.smartvert.model.entity.Calculation;
import org.smartvert.smartvert.model.entity.SavedCalculation;
import org.smartvert.smartvert.repository.AppUserRepository;
import org.smartvert.smartvert.repository.CalculationRepository;
import org.smartvert.smartvert.repository.SavedCalculationRepository;
import org.smartvert.smartvert.service.SavedCalculationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedCalculationServiceImpl implements SavedCalculationService {

    private final SavedCalculationRepository savedCalculationRepository;
    private final CalculationRepository calculationRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public SavedCalculationDTO save(SaveCalculationRequest request) {
        AppUser user = getCurrentAuthenticatedUser();

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
        return appUserRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SavedCalculationDTO toDTO(SavedCalculation saved) {
        Calculation calc = saved.getCalculation();

        CalculationResult.Summary summary = new CalculationResult.Summary(
                calc.getTotalRunningWatts(),
                calc.getPeakSurgeWatts(),
                calc.getDailyEnergyWh()
        );

        CalculationResult.BatteryRecommendation battery = new CalculationResult.BatteryRecommendation(
                null,
                calc.getRecommendedBatteryAh(),
                calc.getRecommendedBatteryKwh(),
                null
        );

        CalculationResult.SolarRecommendation solar = new CalculationResult.SolarRecommendation(
                calc.getRecommendedSolarKw(),
                calc.getPanelCount(),
                null
        );

        CalculationResult.Recommendation recommendation = new CalculationResult.Recommendation(
                calc.getRecommendedInverterKva(),
                battery,
                solar
        );

        CalculationResult result = new CalculationResult(
                calc.getId(),
                calc.getConfiguration().getVersion(),
                summary,
                recommendation,
                Collections.emptyList()
        );

        return new SavedCalculationDTO(
                saved.getId(),
                calc.getId(),
                saved.getLabel(),
                saved.getCreatedAt(),
                result
        );
    }
}
