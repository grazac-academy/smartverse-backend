package org.smartvert.smartvert.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.model.dto.ApiResponse;
import org.smartvert.smartvert.model.dto.ApplianceDTO;
import org.smartvert.smartvert.service.ApplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final ApplianceService applianceService;

    @PostMapping("/appliances")
    public ResponseEntity<ApiResponse<ApplianceDTO>> createAppliance(
            @Valid @RequestBody ApplianceDTO dto) {
        ApplianceDTO created = applianceService.createAppliance(dto);
        return ResponseEntity.ok(ApiResponse.success("Appliance created successfully", created));
    }

    @PutMapping("/appliances/{id}")
    public ResponseEntity<ApiResponse<ApplianceDTO>> updateAppliance(
            @PathVariable UUID id,
            @Valid @RequestBody ApplianceDTO dto) {
        ApplianceDTO updated = applianceService.updateAppliance(id, dto);
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
