package org.smartvert.smartvert.model.dto;

import java.util.UUID;

public record ApplianceCategoryDTO(
    UUID id,
    String code,
    String name,
    Integer displayOrder
) {}
