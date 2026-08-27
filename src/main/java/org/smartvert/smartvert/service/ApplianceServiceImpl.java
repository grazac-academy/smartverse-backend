package org.smartvert.smartvert.service;

import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.exception.ResourceNotFoundException;
import org.smartvert.smartvert.mapper.ApplianceMapper;
import org.smartvert.smartvert.model.dto.ApplianceCategoryDTO;
import org.smartvert.smartvert.model.dto.ApplianceDTO;
import org.smartvert.smartvert.model.entity.Appliance;
import org.smartvert.smartvert.model.entity.ApplianceCategory;
import org.smartvert.smartvert.repository.ApplianceCategoryRepository;
import org.smartvert.smartvert.repository.ApplianceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplianceServiceImpl implements ApplianceService {

    private final ApplianceRepository applianceRepository;
    private final ApplianceCategoryRepository categoryRepository;
    private final ApplianceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ApplianceCategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplianceDTO> getAppliances(UUID categoryId) {
        List<Appliance> list;
        if (categoryId != null) {
            list = applianceRepository.findByCategoryIdAndActiveTrue(categoryId);
        } else {
            list = applianceRepository.findByActiveTrue();
        }
        return list.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApplianceDTO getApplianceById(UUID id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance not found with id: " + id));
        return mapper.toDTO(appliance);
    }

    @Override
    @Transactional
    public ApplianceDTO createAppliance(ApplianceDTO dto) {
        ApplianceCategory category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));

        Appliance appliance = Appliance.builder()
                .category(category)
                .code(dto.code())
                .name(dto.name())
                .defaultWattage(dto.defaultWattage())
                .minWattage(dto.minWattage())
                .maxWattage(dto.maxWattage())
                .defaultVoltage(dto.defaultVoltage())
                .surgeApplicable(dto.surgeApplicable())
                .surgeMultiplier(dto.surgeMultiplier())
                .heavyLoad(dto.heavyLoad())
                .active(true)
                .build();

        Appliance saved = applianceRepository.save(appliance);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ApplianceDTO updateAppliance(UUID id, ApplianceDTO dto) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance not found with id: " + id));

        ApplianceCategory category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));

        appliance.setCategory(category);
        appliance.setCode(dto.code());
        appliance.setName(dto.name());
        appliance.setDefaultWattage(dto.defaultWattage());
        appliance.setMinWattage(dto.minWattage());
        appliance.setMaxWattage(dto.maxWattage());
        appliance.setDefaultVoltage(dto.defaultVoltage());
        appliance.setSurgeApplicable(dto.surgeApplicable());
        appliance.setSurgeMultiplier(dto.surgeMultiplier());
        appliance.setHeavyLoad(dto.heavyLoad());

        Appliance updated = applianceRepository.save(appliance);
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public ApplianceDTO updateApplianceStatus(UUID id, boolean active) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance not found with id: " + id));

        appliance.setActive(active);
        Appliance updated = applianceRepository.save(appliance);
        return mapper.toDTO(updated);
    }
}
