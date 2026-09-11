package org.smartvert.smartvert.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.model.dto.*;
import org.smartvert.smartvert.service.SavedCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/calculations")
@RequiredArgsConstructor
public class UserCalculationController {

    private final SavedCalculationService savedCalculationService;

    @PostMapping
    public ResponseEntity<ApiResponse<SavedCalculationDTO>> saveCalculation(
            @Valid @RequestBody SaveCalculationRequest request) {
        SavedCalculationDTO saved = savedCalculationService.save(request);
        return ResponseEntity.ok(ApiResponse.success("Calculation saved to profile", saved));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedCalculationDTO>>> getAll() {
        List<SavedCalculationDTO> calculations = savedCalculationService.getAllByUser();
        return ResponseEntity.ok(ApiResponse.success("Saved calculations retrieved", calculations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SavedCalculationDTO>> getById(@PathVariable UUID id) {
        SavedCalculationDTO calculation = savedCalculationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Saved calculation retrieved", calculation));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<SavedCalculationDTO>> updateLabel(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSavedCalculationRequest request) {
        SavedCalculationDTO updated = savedCalculationService.updateLabel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Saved calculation updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        savedCalculationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Saved calculation deleted", null));
    }
}
