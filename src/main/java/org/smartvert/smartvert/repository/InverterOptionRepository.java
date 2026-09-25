package org.smartvert.smartvert.repository;

import org.smartvert.smartvert.model.entity.InverterOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InverterOptionRepository extends JpaRepository<InverterOption, UUID> {
    List<InverterOption> findByActiveTrueOrderByRatingKvaAsc();
}
