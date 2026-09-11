package org.smartvert.smartvert.service;

import org.smartvert.smartvert.model.dto.SaveCalculationRequest;
import org.smartvert.smartvert.model.dto.SavedCalculationDTO;
import org.smartvert.smartvert.model.dto.UpdateSavedCalculationRequest;

import java.util.List;
import java.util.UUID;

public interface SavedCalculationService {

    SavedCalculationDTO save(SaveCalculationRequest request);

    List<SavedCalculationDTO> getAllByUser();

    SavedCalculationDTO getById(UUID savedCalculationId);

    SavedCalculationDTO updateLabel(UUID savedCalculationId, UpdateSavedCalculationRequest request);

    void delete(UUID savedCalculationId);
}
