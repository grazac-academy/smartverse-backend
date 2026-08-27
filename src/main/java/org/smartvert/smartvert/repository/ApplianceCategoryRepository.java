package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.ApplianceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplianceCategoryRepository extends JpaRepository<ApplianceCategory, UUID> {
    Optional<ApplianceCategory> findByCode(String code);
    List<ApplianceCategory> findAllByOrderByDisplayOrderAsc();
}
