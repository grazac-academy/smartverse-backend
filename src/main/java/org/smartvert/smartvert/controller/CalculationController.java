package org.smartvert.smartvert.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.model.dto.ApiResponse;
import org.smartvert.smartvert.model.dto.CalculationRequest;
import org.smartvert.smartvert.model.dto.CalculationResult;
import org.smartvert.smartvert.service.CalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/calculations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CalculationController {

    private final CalculationService calculationService;

    @PostMapping
    public ResponseEntity<ApiResponse<CalculationResult>> calculate(
            @Valid @RequestBody CalculationRequest request) {
        CalculationResult result = calculationService.calculate(request);
        return ResponseEntity.ok(ApiResponse.success("Calculation completed successfully", result));
    }
}
