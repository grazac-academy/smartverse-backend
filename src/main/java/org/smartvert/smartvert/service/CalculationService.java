package org.smartvert.smartvert.service;

import org.smartvert.smartvert.model.dto.CalculationRequest;
import org.smartvert.smartvert.model.dto.CalculationResult;

public interface CalculationService {
    CalculationResult calculate(CalculationRequest request);
}
