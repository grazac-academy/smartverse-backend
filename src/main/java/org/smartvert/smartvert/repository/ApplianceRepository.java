package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.Appliance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplianceRepository extends JpaRepository<Appliance, UUID> {
    Optional<Appliance> findByCode(String code);
    List<Appliance> findByActiveTrue();
    List<Appliance> findByCategoryIdAndActiveTrue(UUID categoryId);
}
