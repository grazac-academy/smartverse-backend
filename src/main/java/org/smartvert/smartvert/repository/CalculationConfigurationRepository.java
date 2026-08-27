package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.CalculationConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalculationConfigurationRepository extends JpaRepository<CalculationConfiguration, UUID> {
    Optional<CalculationConfiguration> findByVersion(String version);
    Optional<CalculationConfiguration> findFirstByActiveTrueOrderByCreatedAtDesc();
}
