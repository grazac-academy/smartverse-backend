package org.smartvert.smartvert.controller;

import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.model.dto.ApiResponse;
import org.smartvert.smartvert.model.dto.ApplianceCategoryDTO;
import org.smartvert.smartvert.model.dto.ApplianceDTO;
import org.smartvert.smartvert.service.ApplianceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApplianceController {

    private final ApplianceService applianceService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ApplianceCategoryDTO>>> getCategories() {
        List<ApplianceCategoryDTO> categories = applianceService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @GetMapping("/appliances")
    public ResponseEntity<ApiResponse<List<ApplianceDTO>>> getAppliances(
            @RequestParam(value = "categoryId", required = false) UUID categoryId) {
        List<ApplianceDTO> appliances = applianceService.getAppliances(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Appliances retrieved successfully", appliances));
    }

    @GetMapping("/appliances/{id}")
    public ResponseEntity<ApiResponse<ApplianceDTO>> getAppliance(@PathVariable UUID id) {
        ApplianceDTO appliance = applianceService.getApplianceById(id);
        return ResponseEntity.ok(ApiResponse.success("Appliance retrieved successfully", appliance));
    }
}
