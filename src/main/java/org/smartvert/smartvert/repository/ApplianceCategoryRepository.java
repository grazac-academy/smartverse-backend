package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.ApplianceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplianceCategoryRepository extends JpaRepository<ApplianceCategory, UUID> {
    Optional<ApplianceCategory> findByCode(String code);
    List<ApplianceCategory> findAllByOrderByDisplayOrderAsc();
}
