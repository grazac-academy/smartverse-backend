package org.smartvert.smartvert.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.model.dto.ApiResponse;
import org.smartvert.smartvert.model.dto.ApplianceDTO;
import org.smartvert.smartvert.service.ApplianceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ApplianceService applianceService;

    @PostMapping(value = "/appliances", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ApplianceDTO>> createApplianceJson(
            @Valid @RequestBody ApplianceDTO dto) {
        ApplianceDTO created = applianceService.createAppliance(dto, null);
        return ResponseEntity.ok(ApiResponse.success("Appliance created successfully", created));
    }

    @PostMapping(value = "/appliances", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ApplianceDTO>> createApplianceMultipart(
            @RequestParam("name") String name,
            @RequestParam("code") String code,
            @RequestParam("categoryId") UUID categoryId,
            @RequestParam(value = "defaultWattage", required = false) BigDecimal defaultWattage,
            @RequestParam(value = "minWattage", required = false) BigDecimal minWattage,
            @RequestParam(value = "maxWattage", required = false) BigDecimal maxWattage,
            @RequestParam(value = "defaultVoltage", required = false, defaultValue = "220") Integer defaultVoltage,
            @RequestParam(value = "surgeApplicable", required = false, defaultValue = "false") Boolean surgeApplicable,
            @RequestParam(value = "surgeMultiplier", required = false) BigDecimal surgeMultiplier,
            @RequestParam(value = "heavyLoad", required = false, defaultValue = "false") Boolean heavyLoad,
            @RequestParam(value = "active", required = false, defaultValue = "true") Boolean active,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ApplianceDTO dto = new ApplianceDTO(
                null, categoryId, code, name, defaultWattage, minWattage, maxWattage,
                defaultVoltage, surgeApplicable, surgeMultiplier, heavyLoad, active, null
        );
        ApplianceDTO created = applianceService.createAppliance(dto, file);
        return ResponseEntity.ok(ApiResponse.success("Appliance created successfully", created));
    }

    @PutMapping(value = "/appliances/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ApplianceDTO>> updateApplianceJson(
            @PathVariable UUID id,
            @Valid @RequestBody ApplianceDTO dto) {
        ApplianceDTO updated = applianceService.updateAppliance(id, dto, null);
        return ResponseEntity.ok(ApiResponse.success("Appliance updated successfully", updated));
    }

    @PutMapping(value = "/appliances/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ApplianceDTO>> updateApplianceMultipart(
            @PathVariable UUID id,
            @RequestParam("name") String name,
            @RequestParam("code") String code,
            @RequestParam("categoryId") UUID categoryId,
            @RequestParam(value = "defaultWattage", required = false) BigDecimal defaultWattage,
            @RequestParam(value = "minWattage", required = false) BigDecimal minWattage,
            @RequestParam(value = "maxWattage", required = false) BigDecimal maxWattage,
            @RequestParam(value = "defaultVoltage", required = false, defaultValue = "220") Integer defaultVoltage,
            @RequestParam(value = "surgeApplicable", required = false, defaultValue = "false") Boolean surgeApplicable,
            @RequestParam(value = "surgeMultiplier", required = false) BigDecimal surgeMultiplier,
            @RequestParam(value = "heavyLoad", required = false, defaultValue = "false") Boolean heavyLoad,
            @RequestParam(value = "active", required = false, defaultValue = "true") Boolean active,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ApplianceDTO dto = new ApplianceDTO(
                id, categoryId, code, name, defaultWattage, minWattage, maxWattage,
                defaultVoltage, surgeApplicable, surgeMultiplier, heavyLoad, active, null
        );
        ApplianceDTO updated = applianceService.updateAppliance(id, dto, file);
        return ResponseEntity.ok(ApiResponse.success("Appliance updated successfully", updated));
    }

    @PatchMapping("/appliances/{id}/status")
    public ResponseEntity<ApiResponse<ApplianceDTO>> updateStatus(
            @PathVariable UUID id,
            @RequestParam("active") boolean active) {
        ApplianceDTO updated = applianceService.updateApplianceStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Appliance status updated successfully", updated));
    }
}
