package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.Appliance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplianceRepository extends JpaRepository<Appliance, UUID>, JpaSpecificationExecutor<Appliance> {
    Optional<Appliance> findByCode(String code);
    List<Appliance> findByActiveTrue();
    List<Appliance> findByCategoryIdAndActiveTrue(UUID categoryId);
}
