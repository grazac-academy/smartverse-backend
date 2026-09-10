package org.smartvert.smartvert.service;

import org.smartvert.smartvert.model.dto.ApplianceCategoryDTO;
import org.smartvert.smartvert.model.dto.ApplianceDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface ApplianceService {
    List<ApplianceCategoryDTO> getAllCategories();
    List<ApplianceDTO> getAppliances(UUID categoryId);
    ApplianceDTO getApplianceById(UUID id);
    ApplianceDTO createAppliance(ApplianceDTO dto, MultipartFile file);
    ApplianceDTO updateAppliance(UUID id, ApplianceDTO dto, MultipartFile file);
    ApplianceDTO updateApplianceStatus(UUID id, boolean active);
}
