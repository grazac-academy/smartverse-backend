package org.smartvert.smartvert.service;

import org.smartvert.smartvert.model.dto.ApplianceCategoryDTO;
import org.smartvert.smartvert.model.dto.ApplianceDTO;
import java.util.List;
import java.util.UUID;

public interface ApplianceService {
    List<ApplianceCategoryDTO> getAllCategories();
    List<ApplianceDTO> getAppliances(UUID categoryId);
    ApplianceDTO getApplianceById(UUID id);
    ApplianceDTO createAppliance(ApplianceDTO dto);
    ApplianceDTO updateAppliance(UUID id, ApplianceDTO dto);
    ApplianceDTO updateApplianceStatus(UUID id, boolean active);
}
