package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.Calculation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CalculationRepository extends JpaRepository<Calculation, UUID> {
}
