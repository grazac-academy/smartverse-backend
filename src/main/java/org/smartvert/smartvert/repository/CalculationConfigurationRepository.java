package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.CalculationConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CalculationConfigurationRepository extends JpaRepository<CalculationConfiguration, UUID> {
    Optional<CalculationConfiguration> findByVersion(String version);
    Optional<CalculationConfiguration> findFirstByActiveTrueOrderByCreatedAtDesc();
}
