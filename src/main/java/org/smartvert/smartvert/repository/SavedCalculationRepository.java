package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.SavedCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedCalculationRepository extends JpaRepository<SavedCalculation, UUID> {

    List<SavedCalculation> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<SavedCalculation> findByIdAndUserId(UUID id, UUID userId);
}
